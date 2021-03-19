import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents
import GiphyUISDK
import GiphyCoreSDK

final class CoinSendGiftViewController: ModuleViewController<CoinSendGiftPresenter> {
  
  let rootScrollView = RootScrollView()
  
  let formView = CoinSendGiftFormView()
  
  let submitButton = MDCButton.submit
  
  let contactView = ContactView(isSeparatorVisible: false)

    lazy var separatorView: UIView = {
        let separator = UIView()
        separator.backgroundColor = .lightGray
        return separator
    }()

    
  private let didUpdateImageRelay = PublishRelay<GPHMedia?>()
  
  private var didUpdateImageDriver: Driver<GPHMedia?> {
    return didUpdateImageRelay.asDriver(onErrorJustReturn: nil)
  }
  
  override func setupUI() {
    view.addSubview(rootScrollView)
    rootScrollView.contentView.addSubviews(formView,
                                           submitButton,
                                           separatorView,
                                           contactView)
    
    setupDefaultKeyboardHandling()
  }

  override func setupLayout() {
    
    contactView.snp.makeConstraints{
        $0.top.equalToSuperview().offset(10)
        $0.centerX.equalTo(self.view.snp.centerX).offset(-20)
        $0.height.equalTo(87)
    }
    
    separatorView.snp.makeConstraints{
        $0.top.equalTo(contactView.snp.bottom)
        $0.left.equalToSuperview().offset(15)
        $0.right.equalToSuperview().offset(-15)
        $0.height.equalTo(1/UIScreen.main.scale)
    }
    
    rootScrollView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide)
      $0.left.right.bottom.equalToSuperview()
    }
    rootScrollView.contentView.snp.makeConstraints {
      $0.height.greaterThanOrEqualToSuperview()
    }

    formView.snp.makeConstraints {
      $0.top.equalTo(contactView.snp.bottom)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.lessThanOrEqualTo(submitButton.snp.top).offset(-30)
    }
    submitButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  func setupUIBindings() {
    
    presenter.state
        .drive(onNext:{[unowned self] state in
            guard let fromBalance = state.fromCoinBalance, let fromDetails = state.coinDetails else { return }
            self.formView.coinAmountTextFieldView.configureBalance(for: fromBalance, coinDetails: fromDetails)
            let number = (Decimal(string: state.coinAmount) ?? 0) * fromBalance.price
            self.formView.configureUsdView(usd: number)
    })
    .disposed(by: disposeBag)
    
    let fromCoinDriver = presenter.state
      .map { $0.fromCoin?.type }
      .filterNil()
      .distinctUntilChanged()
 
    let fromCoinBalancesDriver = presenter.state
      .map { state in state.fromCoinBalances?.map { $0.type } }
      .filterNil()
      .distinctUntilChanged()
 
    
    Driver.combineLatest(fromCoinDriver, fromCoinBalancesDriver)
      .drive(onNext: { [formView] in
        formView.configure(coin: $0, fromCoins: $1)
      })
      .disposed(by: disposeBag)
    
    
    presenter.state
        .asObservable()
        .map { $0.fromCoinType }
        .filterNil()
        .bind(to: formView.rx.fromCoin)
        .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.coinAmount }
      .bind(to: formView.rx.fromCoinAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
         .asObservable()
         .map { $0.coinAmountError }
           .subscribeOn(MainScheduler.instance)
           .subscribe { [weak self] result in
               guard let error = result.element else { return }
               self?.formView.configureFromError(error: error)
           }.disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .distinctUntilChanged()
      .drive(onNext: { [unowned self] in
        self.title = String(format: localize(L.CoinSendGift.title), $0)
      })
      .disposed(by: disposeBag)
   
    presenter.state
      .asObservable()
      .map { $0.message }
      .bind(to: formView.rx.messageText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.messageError }
      .bind(to: formView.rx.messageErrorText)
      .disposed(by: disposeBag)
    
    didUpdateImageDriver
      .asObservable()
      .bind(to: formView.rx.gifMedia)
      .disposed(by: disposeBag)
    
    formView.rx.addGifTap
      .drive(onNext: { [unowned self] in
        let gphVC = GiphyViewController()
        gphVC.delegate = self
        self.present(gphVC, animated: true, completion: nil)
      })
      .disposed(by: disposeBag)
    
    formView.rx.removeGifTap
      .drive(onNext: { [unowned self] in self.didUpdateImageRelay.accept(nil) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.isAllRequiredFieldsNotEmpty }
      .bind(to: submitButton.rx.isEnabled)
      .disposed(by: disposeBag)
    
    submitButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
    
    
    presenter.state
      .asObservable()
      .map { $0.contact }
      .filterNil()
      .subscribe(onNext: { [weak self] (contact) in
        self?.contactView.update(contact: contact)
      })
      .disposed(by: disposeBag)
    
  }

  override func setupBindings() {
    setupUIBindings()
    
    let updateCoinAmountDriver = formView.rx.fromCoinAmountText.asDriver()
    let updateFromPickerItemDriver = formView.rx.selectFromPickerItem
    let maxFromDriver = formView.rx.maxFromTap
    
    
    let updateMessageDriver = formView.rx.messageText.asDriver()
    let updateImageDriver = didUpdateImageDriver.map { $0?.id }
    let submitDriver = submitButton.rx.tap.asDriver()
    let fromCoinTypeDriver = formView.rx.willChangeFromCoinType
    
    presenter.bind(input: CoinSendGiftPresenter.Input(updateCoinAmount: updateCoinAmountDriver,
                                                      updateFromPickerItem: updateFromPickerItemDriver,
                                                      maxFrom: maxFromDriver,
                                                      updateMessage: updateMessageDriver,
                                                      updateImage: updateImageDriver,
                                                      submit: submitDriver,
                                                      fromCoinType: fromCoinTypeDriver))
  }
}

extension CoinSendGiftViewController: GiphyDelegate {
  func didSelectMedia(giphyViewController: GiphyViewController, media: GPHMedia) {
    didUpdateImageRelay.accept(media)
    
    giphyViewController.dismiss(animated: true, completion: nil)
  }
  
  func didDismiss(controller: GiphyViewController?) {}
}

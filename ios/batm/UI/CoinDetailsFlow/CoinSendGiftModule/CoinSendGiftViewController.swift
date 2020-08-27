import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents
import GiphyUISDK
import GiphyCoreSDK

final class CoinSendGiftViewController: NavigationScreenViewController<CoinSendGiftPresenter> {
  
  let errorView = ErrorView()
  
  let headerView = HeaderView()
  
  let formView = CoinSendGiftFormView()
  
  let nextButton = MDCButton.next
  
  private let didUpdateImageRelay = PublishRelay<GPHMedia?>()
  
  private var didUpdateImageDriver: Driver<GPHMedia?> {
    return didUpdateImageRelay.asDriver(onErrorJustReturn: nil)
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    customView.rootScrollView.contentInsetAdjustmentBehavior = .never
    customView.contentView.addSubviews(errorView,
                                       headerView,
                                       formView,
                                       nextButton)
    
    setupDefaultKeyboardHandling()
  }

  override func setupLayout() {
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(5)
      $0.centerX.equalToSuperview()
    }
    headerView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(25)
      $0.left.equalToSuperview().offset(15)
      $0.right.lessThanOrEqualToSuperview().offset(-15)
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(headerView.snp.bottom).offset(20)
      $0.left.right.equalToSuperview().inset(15)
    }
    nextButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.top.equalTo(formView.snp.bottom).offset(15)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.lessThanOrEqualToSuperview().offset(-30)
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .distinctUntilChanged()
      .drive(onNext: { [customView] in
        customView.setTitle(String(format: localize(L.CoinSendGift.title), $0))
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coinBalance }
      .filterNil()
      .drive(onNext: { [headerView] coinBalance in
        let balanceView = CoinDetailsBalanceValueView()
        balanceView.configure(for: coinBalance)
        
        headerView.removeAll()
        headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withUSD)
        headerView.add(title: localize(L.CoinDetails.balance), valueView: balanceView)
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .drive(onNext: { [formView] in formView.configure(with: $0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.phone }
      .bind(to: formView.rx.phoneText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.currencyAmount }
      .bind(to: formView.rx.currencyText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.coinAmount }
      .bind(to: formView.rx.coinText)
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
      .map { $0.validationState }
      .mapToErrorMessage()
      .drive(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
      })
      .disposed(by: disposeBag)
    
    nextButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updateCountryDriver = formView.rx.country
    let updatePhoneDriver = formView.rx.phoneText.asDriver()
    let updateCurrencyAmountDriver = formView.rx.currencyText.asDriver()
    let updateCoinAmountDriver = formView.rx.coinText.asDriver()
    let updateMessageDriver = formView.rx.messageText.asDriver()
    let updateImageIdDriver = didUpdateImageDriver.map { $0?.id }
    let pastePhoneDriver = formView.rx.pasteTap
    let maxDriver = formView.rx.maxTap
    let nextDriver = nextButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinSendGiftPresenter.Input(back: backDriver,
                                                      updateCountry: updateCountryDriver,
                                                      updatePhone: updatePhoneDriver,
                                                      updateCurrencyAmount: updateCurrencyAmountDriver,
                                                      updateCoinAmount: updateCoinAmountDriver,
                                                      updateMessage: updateMessageDriver,
                                                      updateImageId: updateImageIdDriver,
                                                      pastePhone: pastePhoneDriver,
                                                      max: maxDriver,
                                                      next: nextDriver))
  }
}

extension CoinSendGiftViewController: GiphyDelegate {
  func didSelectMedia(giphyViewController: GiphyViewController, media: GPHMedia) {
    didUpdateImageRelay.accept(media)
    
    giphyViewController.dismiss(animated: true, completion: nil)
  }
  
  func didDismiss(controller: GiphyViewController?) {}
}

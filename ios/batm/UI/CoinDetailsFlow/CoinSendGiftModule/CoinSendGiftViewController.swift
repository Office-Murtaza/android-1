import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents
import GiphyUISDK
import GiphyCoreSDK

final class CoinSendGiftViewController: ModuleViewController<CoinSendGiftPresenter> {
  
  let rootScrollView = RootScrollView()
  
  let headerView = HeaderView()
  
  let formView = CoinSendGiftFormView()
  
  let submitButton = MDCButton.submit
  
  private let didUpdateImageRelay = PublishRelay<GPHMedia?>()
  
  private var didUpdateImageDriver: Driver<GPHMedia?> {
    return didUpdateImageRelay.asDriver(onErrorJustReturn: nil)
  }
  
  override var shouldShowNavigationBar: Bool { return true }
  
  override func setupUI() {
    view.addSubview(rootScrollView)
    rootScrollView.contentView.addSubviews(headerView,
                                           formView,
                                           submitButton)
    
    setupDefaultKeyboardHandling()
  }

  override func setupLayout() {
    rootScrollView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide)
      $0.left.right.bottom.equalToSuperview()
    }
    rootScrollView.contentView.snp.makeConstraints {
      $0.height.greaterThanOrEqualToSuperview()
    }
    headerView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(25)
      $0.left.equalToSuperview().offset(15)
      $0.right.lessThanOrEqualToSuperview().offset(-15)
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(headerView.snp.bottom).offset(30)
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
      .map { $0.coin?.type.code }
      .filterNil()
      .distinctUntilChanged()
      .drive(onNext: { [unowned self] in
        self.title = String(format: localize(L.CoinSendGift.title), $0)
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coinBalance }
      .filterNil()
      .drive(onNext: { [headerView] coinBalance in
        let amountView = CryptoFiatAmountView()
        amountView.configure(for: coinBalance)
        
        headerView.removeAll()
        headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withUSD)
        headerView.add(title: localize(L.CoinDetails.balance), valueView: amountView)
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
      .map { $0.fiatAmount }
      .bind(to: formView.rx.fiatAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.coinAmount }
      .bind(to: formView.rx.coinAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.message }
      .bind(to: formView.rx.messageText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.phoneError }
      .bind(to: formView.rx.phoneErrorText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.coinAmountError }
      .bind(to: formView.rx.coinAmountErrorText)
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
  }

  override func setupBindings() {
    setupUIBindings()
    
    let updatePhoneDriver = formView.rx.phoneText.asDriver()
    let updateCoinAmountDriver = formView.rx.coinAmountText.asDriver()
    let updateMessageDriver = formView.rx.messageText.asDriver()
    let updateImageIdDriver = didUpdateImageDriver.map { $0?.id }
    let maxDriver = formView.rx.maxTap
    let submitDriver = submitButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinSendGiftPresenter.Input(updatePhone: updatePhoneDriver,
                                                      updateCoinAmount: updateCoinAmountDriver,
                                                      updateMessage: updateMessageDriver,
                                                      updateImageId: updateImageIdDriver,
                                                      max: maxDriver,
                                                      submit: submitDriver))
  }
}

extension CoinSendGiftViewController: GiphyDelegate {
  func didSelectMedia(giphyViewController: GiphyViewController, media: GPHMedia) {
    didUpdateImageRelay.accept(media)
    
    giphyViewController.dismiss(animated: true, completion: nil)
  }
  
  func didDismiss(controller: GiphyViewController?) {}
}

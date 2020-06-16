import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class BuySellTradeDetailsViewController: NavigationScreenViewController<BuySellTradeDetailsPresenter> {
  
  let errorView = ErrorView()
  
  let headerView = BuySellTradeDetailsHeaderView()
  
  let formView = BuySellTradeDetailsFormView()
  
  let sendRequestButton = MDCButton.sendRequest
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    customView.rootScrollView.contentInsetAdjustmentBehavior = .never
    customView.contentView.addSubviews(errorView,
                                       headerView,
                                       formView,
                                       sendRequestButton)
    
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
    sendRequestButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.top.equalTo(formView.snp.bottom).offset(25)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.lessThanOrEqualToSuperview().offset(-30)
    }
  }
  
  func setupUIBindings() {
    let tradeTypeDriver = presenter.state
    .map { $0.type?.verboseValue }
    .filterNil()
    
    let coinCodeDriver = presenter.state
      .map { $0.coinBalance?.type.code }
      .filterNil()
    
    Driver.combineLatest(tradeTypeDriver, coinCodeDriver)
      .drive(onNext: { [customView] in
        let title = String(format: localize(L.BuySellTradeDetails.title), $0, $1)
        customView.setTitle(title)
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.trade }
      .filterNil()
      .drive(onNext: { [headerView] in headerView.configure(for: $0) })
      .disposed(by: disposeBag)
    
    coinCodeDriver
      .drive(onNext: { [formView] in formView.configure(with: $0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.coinAmount }
      .bind(to: formView.rx.coinText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.currencyAmount }
      .bind(to: formView.rx.currencyText)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.validationState }
      .mapToErrorMessage()
      .drive(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
      })
      .disposed(by: disposeBag)
    
    sendRequestButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updateCurrencyAmountDriver = formView.rx.currencyText.asDriver()
    let updateCoinAmountDriver = formView.rx.coinText.asDriver()
    let maxDriver = formView.rx.maxTap
    let sendRequestDriver = sendRequestButton.rx.tap.asDriver()
    
    presenter.bind(input: BuySellTradeDetailsPresenter.Input(back: backDriver,
                                                             updateCoinAmount: updateCoinAmountDriver,
                                                             updateCurrencyAmount: updateCurrencyAmountDriver,
                                                             max: maxDriver,
                                                             sendRequest: sendRequestDriver))
  }
}

import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinExchangeViewController: NavigationScreenViewController<CoinExchangePresenter> {
  
  let errorView = ErrorView()
  
  let headerView = HeaderView()
  
  let formView = CoinExchangeFormView()
  
  let nextButton = MDCButton.next
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    customView.rootScrollView.canCancelContentTouches = false
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
      $0.bottom.equalToSuperview().inset(25)
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .map { $0.fromCoinBalance }
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
      .map { $0.fromCoin?.type.code }
      .filterNil()
      .distinctUntilChanged()
      .drive(onNext: { [customView] in
        customView.setTitle(String(format: localize(L.CoinExchange.title), $0))
      })
      .disposed(by: disposeBag)
    
    let fromCoinDriver = presenter.state
      .map { $0.fromCoin?.type }
      .filterNil()
      .distinctUntilChanged()
    
    let otherCoinBalancesDriver = presenter.state
      .map { state in state.otherCoinBalances?.map { $0.type } }
      .filterNil()
      .distinctUntilChanged()
    
    Driver.combineLatest(fromCoinDriver, otherCoinBalancesDriver)
      .drive(onNext: { [formView] in formView.configure(for: $0, and: $1) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.fromCoinAmount }
      .bind(to: formView.rx.fromCoinAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.toCoinType }
      .filterNil()
      .bind(to: formView.rx.toCoin)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.toCoinAmount }
      .bind(to: formView.rx.toCoinAmountText)
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
      .drive(onNext: { [unowned self] in self.view.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updateFromCoinAmountDriver = formView.rx.fromCoinAmountText.asDriver()
    let updatePickerItemDriver = formView.rx.selectPickerItem
    let maxDriver = formView.rx.maxTap
    let nextDriver = nextButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinExchangePresenter.Input(back: backDriver,
                                                      updateFromCoinAmount: updateFromCoinAmountDriver,
                                                      updatePickerItem: updatePickerItemDriver,
                                                      max: maxDriver,
                                                      next: nextDriver))
  }
}

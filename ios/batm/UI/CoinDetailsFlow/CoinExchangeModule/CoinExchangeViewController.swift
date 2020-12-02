import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinExchangeViewController: ModuleViewController<CoinExchangePresenter> {
  
  let rootScrollView = RootScrollView()
  
  let headerView = HeaderView()
  
  let formView = CoinExchangeFormView()
  
  let submitButton = MDCButton.submit

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
      $0.height.equalToSuperview()
    }
    headerView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(25)
      $0.left.equalToSuperview().offset(15)
      $0.right.lessThanOrEqualToSuperview().offset(-15)
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(headerView.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(15)
    }
    submitButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .map { $0.fromCoinBalance }
      .filterNil()
      .drive(onNext: { [headerView] coinBalance in
        let amountView = CryptoFiatAmountView()
        amountView.configure(for: coinBalance)
        let reservedView = CryptoFiatAmountView()
        reservedView.configure(for: coinBalance, useReserved: true)
        
        headerView.removeAll()
        headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withDollarSign)
        headerView.add(title: localize(L.CoinDetails.balance), valueView: amountView)
        headerView.add(title: localize(L.CoinDetails.reserved), valueView: reservedView)
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.fromCoin?.type.code }
      .filterNil()
      .distinctUntilChanged()
      .drive(onNext: { [unowned self] in
        self.title = String(format: localize(L.CoinExchange.title), $0)
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
    
    let feeDriver = presenter.state
    .map { $0.coinDetails?.txFee }
    
    Driver.combineLatest(fromCoinDriver, otherCoinBalancesDriver, feeDriver)
      .drive(onNext: { [formView] in formView.configure(coin: $0, otherCoins: $1, fee: $2) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.fromCoinAmount }
      .bind(to: formView.rx.fromCoinAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.fromCoinFiatAmount }
      .bind(to: formView.rx.fromCoinFiatAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.toCoinType }
      .filterNil()
      .bind(to: formView.rx.toCoin)
      .disposed(by: disposeBag)
    
//    presenter.state
//      .asObservable()
//      .map { $0.toCoinAmount }
//      .bind(to: formView.rx.toCoinAmountText)
//      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.fromCoinAmountError }
      .bind(to: formView.rx.fromCoinAmountErrorText)
      .disposed(by: disposeBag)

    presenter.state
      .asObservable()
      .map { $0.toCoinTypeError }
      .bind(to: formView.rx.toCoinErrorText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.isAllFieldsNotEmpty }
      .bind(to: submitButton.rx.isEnabled)
      .disposed(by: disposeBag)
    
    submitButton.rx.tap.asDriver()
      .drive(onNext: { [unowned self] in self.view.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let updateFromCoinAmountDriver = formView.rx.fromCoinAmountText.asDriver()
    let updatePickerItemDriver = formView.rx.selectPickerItem
    let maxDriver = formView.rx.maxTap
    let submitDriver = submitButton.rx.tap.asDriver()
    let toCoinTypeDriver = formView.rx.willChangeCoinType
    
    presenter.bind(input: CoinExchangePresenter.Input(updateFromCoinAmount: updateFromCoinAmountDriver,
                                                      updatePickerItem: updatePickerItemDriver,
                                                      toCoinType: toCoinTypeDriver,
                                                      max: maxDriver,
                                                      submit: submitDriver))
  }
}

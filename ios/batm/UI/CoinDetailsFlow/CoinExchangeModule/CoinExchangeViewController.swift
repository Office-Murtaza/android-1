import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinExchangeViewController: ModuleViewController<CoinExchangePresenter> {
  
  let rootScrollView = RootScrollView()
    
  let formView = CoinExchangeFormView()
  
  let submitButton = MDCButton.submit

  override func setupUI() {
    view.addSubview(rootScrollView)
    rootScrollView.contentView.addSubviews(formView,
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
    formView.snp.makeConstraints {
      $0.top.equalToSuperview()
      $0.left.right.equalToSuperview()
      $0.height.equalTo(300)
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
      .drive(onNext: { [unowned self] coinBalance in
        self.formView.fromCoinView.configurBalance(for: coinBalance)
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.toCoinBalance }
      .filterNil()
      .drive(onNext: { [unowned self] coinBalance in
        self.formView.toCoinView.configurBalance(for: coinBalance)
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
    
    let fromCoinBalancesDriver = presenter.state
      .map { state in state.fromCoinBalances?.map { $0.type } }
      .filterNil()
      .distinctUntilChanged()
    
    let toCoinBalancesDriver = presenter.state
      .map { state in state.toCoinBalances?.map { $0.type } }
      .filterNil()
      .distinctUntilChanged()
    
    let feeDriver = presenter.state
    .map { $0.coinDetails?.txFee }
    
    Driver.combineLatest(fromCoinDriver, fromCoinBalancesDriver, toCoinBalancesDriver, feeDriver)
      .drive(onNext: { [formView] in
              formView.configure(coin: $0, fromCoins: $1, toCoins: $2, fee: $3)
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.fromCoinAmount }
      .bind(to: formView.rx.fromCoinAmountText)
      .disposed(by: disposeBag)
    
    
    presenter.state
      .asObservable()
      .map { $0.fromCoinAmount }
      .bind(to: formView.rx.fromCoinAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
        .asObservable()
        .subscribeOn(MainScheduler.instance)
        .subscribe { [weak self] result in
            guard let fee = result.element?.platformFee else { return }
            self?.formView.configureFeeView(fee: fee)
        }.disposed(by: disposeBag)
    
    
//    presenter.state
//      .asObservable()
//      .map { $0.fromCoinFiatAmount }
//      .bind(to: formView.rx.fromCoinFiatAmountText)
//      .disposed(by: disposeBag)
    
    presenter.state
        .asObservable()
        .subscribeOn(MainScheduler.instance)
        .subscribe { [weak self] (state) in
        guard let fromRate = state.element?.fromRate, let toRate = state.element?.toRate else { return }
        self?.formView.configureRateView(fromCoin: fromRate, toCoin: toRate)
    }.disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.toCoinType }
      .filterNil()
      .bind(to: formView.rx.toCoin)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.fromCoinType }
      .filterNil()
      .bind(to: formView.rx.fromCoin)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.toCoinAmount }
      .bind(to: formView.rx.toCoinAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.fromCoinAmountError }
        .subscribeOn(MainScheduler.instance)
        .subscribe { [weak self] result in
            guard let error = result.element else { return }
            self?.formView.configureFromError(error: error )
        }.disposed(by: disposeBag)

    presenter.state
      .asObservable()
      .map { $0.toCoinTypeError }
        .subscribeOn(MainScheduler.instance)
        .subscribe { [weak self] result in
            guard let error = result.element else { return }
            self?.formView.configureToError(error: error )
        }.disposed(by: disposeBag)
    
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
    
    presenter.coinTypeDidChange.observeOn(MainScheduler.instance).subscribe({ [weak self] _ in
        self?.formView.fromCoinView.amountTextField.text = nil
        self?.formView.toCoinView.amountTextField.text = nil
    })
    .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let updateFromCoinAmountDriver = formView.rx.fromCoinAmountText.asDriver()
    let updateToCoinAmountDriver = formView.rx.toCoinAmountText.asDriver()
    let updateToPickerItemDriver = formView.rx.selectToPickerItem
    let updateFromPickerItemDriver = formView.rx.selectFromPickerItem
    let maxFromDriver = formView.rx.maxFromTap
    let maxToDriver = formView.rx.maxToTap
    let submitDriver = submitButton.rx.tap.asDriver()
    let toCoinTypeDriver = formView.rx.willChangeToCoinType
    let fromCoinTypeDriver = formView.rx.willChangeFromCoinType
    let swapDriver = formView.rx.swapButtonDidPushed.asDriver()
    
    presenter.bind(input: CoinExchangePresenter.Input(updateFromCoinAmount: updateFromCoinAmountDriver,
                                                      updateToCoinAmount: updateToCoinAmountDriver,
                                                      updateToPickerItem: updateToPickerItemDriver,
                                                      updateFromPickerItem: updateFromPickerItemDriver,
                                                      toCoinType: toCoinTypeDriver,
                                                      fromCoinType: fromCoinTypeDriver,
                                                      maxFrom: maxFromDriver,
                                                      maxTo: maxToDriver,
                                                      submit: submitDriver,
                                                      swap: swapDriver))
  }
}

extension ObservableType {

  func withPrevious() -> Observable<(E?, E)> {
    return scan([], accumulator: { (previous, current) in
        Array(previous + [current]).suffix(2)
      })
      .map({ (arr) -> (previous: E?, current: E) in
        (arr.count > 1 ? arr.first : nil, arr.last!)
      })
  }
}

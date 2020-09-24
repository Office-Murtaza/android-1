import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class RecallViewController: NavigationScreenViewController<RecallPresenter> {
  
  let errorView = ErrorView()
  
  let headerView = HeaderView()
  
  let formView = ReserveFormView()
  
  let recallButton = MDCButton.recall
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override var shouldShowNavigationBar: Bool { return false }
  
  override func setupUI() {
    customView.rootScrollView.contentInsetAdjustmentBehavior = .never
    customView.contentView.addSubviews(errorView,
                                       headerView,
                                       formView,
                                       recallButton)
    
    setupDefaultKeyboardHandling()
  }
  
  override func setupLayout() {
    customView.contentView.snp.makeConstraints {
      $0.height.equalToSuperview()
    }
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
    recallButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.top.equalTo(formView.snp.bottom).offset(20)
      $0.left.right.equalToSuperview().inset(15)
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .distinctUntilChanged()
      .drive(onNext: { [customView] in
        customView.setTitle(String(format: localize(L.Recall.title), $0))
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coinBalance }
      .filterNil()
      .drive(onNext: { [headerView] coinBalance in
        let amountView = CryptoFiatAmountView()
        amountView.configure(for: coinBalance)
        
        let reservedAmountView = CryptoFiatAmountView()
        reservedAmountView.configure(for: coinBalance, useReserved: true)
        
        headerView.removeAll()
        headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withDollarSign)
        headerView.add(title: localize(L.CoinDetails.balance), valueView: amountView)
        headerView.add(title: localize(L.Trades.reserved), valueView: reservedAmountView)
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .drive(onNext: { [formView] in formView.configure(with: $0) })
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
    
    presenter.state
      .map { $0.validationState }
      .mapToErrorMessage()
      .drive(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
      })
      .disposed(by: disposeBag)
    
    recallButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updateCurrencyAmountDriver = formView.rx.currencyText.asDriver(onErrorDriveWith: .empty())
    let updateCoinAmountDriver = formView.rx.coinText.asDriver(onErrorDriveWith: .empty())
    let maxDriver = formView.rx.maxTap
    let recallDriver = recallButton.rx.tap.asDriver()
    
    presenter.bind(input: RecallPresenter.Input(back: backDriver,
                                                updateCurrencyAmount: updateCurrencyAmountDriver,
                                                updateCoinAmount: updateCoinAmountDriver,
                                                max: maxDriver,
                                                recall: recallDriver))
  }
}

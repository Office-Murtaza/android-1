import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinSellViewController: NavigationScreenViewController<CoinSellPresenter> {
  
  let errorView = ErrorView()
  
  let headerView = HeaderView()
  
  let formView = CoinSellFormView()
  
  let nextButton = MDCButton.next
  
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
    nextButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.top.equalTo(formView.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(15)
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .distinctUntilChanged()
      .drive(onNext: { [customView] in
        customView.setTitle(String(format: localize(L.CoinSell.title), $0))
      })
      .disposed(by: disposeBag)
    
    let coinBalanceDriver =  presenter.state
      .map { $0.coinBalance }
      .filterNil()
    
    let detailsDriver = presenter.state
      .map { $0.details }
      .filterNil()
   
    Driver.combineLatest(coinBalanceDriver, detailsDriver)
      .drive(onNext: { [headerView] coinBalance, details in
        let balanceView = CoinDetailsBalanceValueView()
        balanceView.configure(for: coinBalance)
        
        headerView.removeAll()
        headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withUSD)
        headerView.add(title: localize(L.CoinDetails.balance), valueView: balanceView)
        headerView.add(title: localize(L.CoinSell.dailyLimit), value: details.dailyLimit.fiatFormatted.withUSD)
        headerView.add(title: localize(L.CoinSell.txLimit), value: details.transactionLimit.fiatFormatted.withUSD)
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
    
    nextButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updateFromAnotherAddressDriver = formView.rx.isAnotherAddress
    let updateCurrencyAmountDriver = formView.rx.currencyText.asDriver(onErrorDriveWith: .empty())
    let maxDriver = formView.rx.maxTap
    let nextDriver = nextButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinSellPresenter.Input(back: backDriver,
                                                  updateFromAnotherAddress: updateFromAnotherAddressDriver,
                                                  updateCurrencyAmount: updateCurrencyAmountDriver,
                                                  max: maxDriver,
                                                  next: nextDriver))
  }
}

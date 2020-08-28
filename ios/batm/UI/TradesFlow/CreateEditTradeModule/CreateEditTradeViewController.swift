import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CreateEditTradeViewController: NavigationScreenViewController<CreateEditTradePresenter> {
  
  let errorView = ErrorView()
  
  let headerView = HeaderView()
  
  let tradeTypeView = CreateEditTradeTypeView()
  
  let formView = CreateEditTradeFormView()
  
  let createButton = MDCButton.create
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    customView.rootScrollView.contentInsetAdjustmentBehavior = .never
    customView.contentView.addSubviews(errorView,
                                       headerView,
                                       formView,
                                       createButton)
    
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
    createButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.top.equalTo(formView.snp.bottom).offset(25)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.lessThanOrEqualToSuperview().offset(-30)
    }
  }
  
  func setupUIBindings() {
    let coinBalanceDriver =  presenter.state
         .map { $0.coinBalance }
         .filterNil()
         .distinctUntilChanged()
    
    let tradeDriver = presenter.state
      .map { $0.trade }
      .distinctUntilChanged()
    
    Driver.combineLatest(coinBalanceDriver, tradeDriver)
      .drive(onNext: { [customView] in
        let title: String
        
        if $1 == nil {
          title = String(format: localize(L.CreateEditTrade.createTitle), $0.type.code)
        } else {
          title = String(format: localize(L.CreateEditTrade.editTitle), $0.type.code)
        }
        
        customView.setTitle(title)
      })
      .disposed(by: disposeBag)
    
    Driver.combineLatest(coinBalanceDriver, tradeDriver)
      .drive(onNext: { [headerView, tradeTypeView] coinBalance, trade in
        let balanceView = CoinDetailsBalanceValueView()
        balanceView.configure(for: coinBalance)
        
        let reservedBalanceView = CoinDetailsBalanceValueView()
        reservedBalanceView.configure(for: coinBalance, useReserved: true)
        
        trade.flatMap { tradeTypeView.configure(for: $0) }
        
        headerView.removeAll()
        headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withUSD)
        headerView.add(title: localize(L.CoinDetails.balance), valueView: balanceView)
        headerView.add(title: localize(L.Trades.reserved), valueView: reservedBalanceView)
        headerView.add(title: localize(L.CreateEditTrade.type), valueView: tradeTypeView)
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.payment }
      .bind(to: formView.rx.paymentText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.margin }
      .bind(to: formView.rx.marginText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.minLimit }
      .bind(to: formView.rx.minLimitText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.maxLimit }
      .bind(to: formView.rx.maxLimitText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.terms }
      .bind(to: formView.rx.termsText)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.validationState }
      .mapToErrorMessage()
      .drive(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
      })
      .disposed(by: disposeBag)
    
    createButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updateSelectedTypeDriver = tradeTypeView.rx.acceptedType
    let updatePaymentDriver = formView.rx.paymentText.asDriver()
    let updateMarginDriver = formView.rx.marginText.asDriver()
    let updateMinLimitDriver = formView.rx.minLimitText.asDriver()
    let updateMaxLimitDriver = formView.rx.maxLimitText.asDriver()
    let updateTermsDriver = formView.rx.termsText.asDriver()
    let createDriver = createButton.rx.tap.asDriver()
    
    presenter.bind(input: CreateEditTradePresenter.Input(back: backDriver,
                                                         updateSelectedType: updateSelectedTypeDriver,
                                                         updatePayment: updatePaymentDriver,
                                                         updateMargin: updateMarginDriver,
                                                         updateMinLimit: updateMinLimitDriver,
                                                         updateMaxLimit: updateMaxLimitDriver,
                                                         updateTerms: updateTermsDriver,
                                                         create: createDriver))
  }
}

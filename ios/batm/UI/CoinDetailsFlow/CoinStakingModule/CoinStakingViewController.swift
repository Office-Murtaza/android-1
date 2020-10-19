import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinStakingViewController: ModuleViewController<CoinStakingPresenter> {
  
  let rootScrollView = RootScrollView()
  
  let headerView = HeaderView()
  
  let formView = CoinStakingFormView()
  
  let errorLabel: UILabel = {
    let label = UILabel()
    label.textColor = .tomato
    label.textAlignment = .center
    label.font = .systemFont(ofSize: 16)
    label.numberOfLines = 0
    return label
  }()
  
  let buttonsStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let createButton = MDCButton.create
  
  let cancelButton = MDCButton.cancel
  
  let withdrawButton = MDCButton.withdraw
  
  var buttons: [MDCButton] {
    return [createButton, cancelButton, withdrawButton]
  }
  
  override func setupUI() {
    view.addSubview(rootScrollView)
    rootScrollView.contentView.addSubviews(headerView,
                                           formView,
                                           errorLabel,
                                           buttonsStackView)
    
    buttonsStackView.addArrangedSubviews(createButton,
                                         cancelButton,
                                         withdrawButton)
    
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
    }
    errorLabel.snp.makeConstraints {
      $0.top.greaterThanOrEqualTo(formView.snp.bottom).offset(25)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalTo(buttonsStackView.snp.top).offset(-25)
    }
    buttonsStackView.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
    buttons.forEach {
      $0.snp.makeConstraints {
        $0.height.equalTo(50)
      }
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .drive(onNext: { [unowned self] in
        self.title = String(format: localize(L.CoinStaking.title), $0)
      })
      .disposed(by: disposeBag)
    
    let coinBalanceDriver = presenter.state
      .map { $0.coinBalance }
      .filterNil()
    
    let stakeDetailsDriver = presenter.state
      .map { $0.stakeDetails }
      .filterNil()
    
    let feeDriver = presenter.state
      .map { $0.coinDetails?.txFee }
    
    Driver.combineLatest(coinBalanceDriver, stakeDetailsDriver)
      .drive(onNext: { [headerView] coinBalance, stakeDetails in
        let amountView = CryptoFiatAmountView()
        amountView.configure(for: coinBalance)
        
        headerView.removeAll()
        headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withDollarSign)
        headerView.add(title: localize(L.CoinDetails.balance), valueView: amountView)
        
        if stakeDetails.created {
          headerView.add(title: localize(L.CoinStaking.Header.Amount.title),
                         value: "\(stakeDetails.amount ?? 0) \(coinBalance.type.code)")
          
          let statusView = StatusView()
          statusView.configure(text: stakeDetails.status.verboseValue, color: stakeDetails.status.associatedColor)
          
          headerView.add(title: localize(L.CoinStaking.Header.Status.title), valueView: statusView)
          
          headerView.add(title: localize(L.CoinStaking.Header.Reward.title),
                         value: "\(stakeDetails.rewardAmount ?? 0) \(coinBalance.type.code), \(stakeDetails.rewardPercent ?? 0)%")
          
          headerView.add(title: localize(L.CoinStaking.Header.RewardAnnual.title),
                         value: "\(stakeDetails.rewardAnnualAmount ?? 0) \(coinBalance.type.code), \(stakeDetails.rewardAnnualPercent)%")
        } else {
          headerView.add(title: localize(L.CoinStaking.Header.RewardAnnual.title),
                         value: "\(stakeDetails.rewardAnnualPercent)%")
        }
        
        stakeDetails.createDateString.flatMap {
          headerView.add(title: localize(L.CoinStaking.Header.CreateDate.title),
                         value: $0)
        }
        
        stakeDetails.cancelDateString.flatMap {
          headerView.add(title: localize(L.CoinStaking.Header.CancelDate.title),
                         value: $0)
        }
        
        if stakeDetails.created {
          headerView.add(title: localize(L.CoinStaking.Header.Duration.title),
                         value: String(format: localize(L.CoinStaking.Header.Duration.value), stakeDetails.duration ?? 0))
        }
        
        headerView.add(title: localize(L.CoinStaking.Header.CancelPeriod.title),
                       value: String(format: localize(L.CoinStaking.Header.CancelPeriod.value), stakeDetails.cancelPeriod))
        
        if stakeDetails.canceled {
          headerView.add(title: localize(L.CoinStaking.Header.UntilWithdraw.title),
          value: String(format: localize(L.CoinStaking.Header.UntilWithdraw.value), stakeDetails.untilWithdraw ?? 0))
        }
      })
      .disposed(by: disposeBag)
    
    Driver.combineLatest(coinBalanceDriver, stakeDetailsDriver, feeDriver)
      .drive(onNext: { [formView] in formView.configure(coinType: $0.type, stakeDetails: $1, fee: $2) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.coinAmount }
      .bind(to: formView.rx.coinAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.fiatAmount }
      .bind(to: formView.rx.fiatAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.coinAmountError }
      .bind(to: formView.rx.coinAmountErrorText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.coinAmountError }
      .bind(to: errorLabel.rx.text)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.shouldShowCreateButton }
      .bind(to: errorLabel.rx.isHidden)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { !$0.shouldShowCreateButton }
      .bind(to: createButton.rx.isHidden)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.isAllFieldsNotEmpty }
      .bind(to: createButton.rx.isEnabled)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { !$0.shouldShowCancelButton }
      .bind(to: cancelButton.rx.isHidden)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { !$0.shouldShowWithdrawButton }
      .bind(to: withdrawButton.rx.isHidden)
      .disposed(by: disposeBag)
    
    Driver.merge(buttons.map { $0.rx.tap.asDriver() })
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let updateCoinAmountDriver = formView.rx.coinAmountText.asDriver(onErrorDriveWith: .empty())
    let maxDriver = formView.rx.maxTap
    let createDriver = createButton.rx.tap.asDriver()
    let cancelDriver = cancelButton.rx.tap.asDriver()
    let withdrawDriver = withdrawButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinStakingPresenter.Input(updateCoinAmount: updateCoinAmountDriver,
                                                     max: maxDriver,
                                                     create: createDriver,
                                                     cancel: cancelDriver,
                                                     withdraw: withdrawDriver))
  }
}

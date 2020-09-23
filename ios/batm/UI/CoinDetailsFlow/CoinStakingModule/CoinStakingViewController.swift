import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinStakingViewController: ModuleViewController<CoinStakingPresenter> {
  
  let rootScrollView = RootScrollView()
  
  let headerView = HeaderView()
  
  let formView = CoinStakingFormView()
  
  let stakeButton = MDCButton.stake
  
  let unstakeButton = MDCButton.unstake
  
  override func setupUI() {
    view.addSubview(rootScrollView)
    rootScrollView.contentView.addSubviews(headerView,
                                           formView,
                                           stakeButton,
                                           unstakeButton)
    
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
    [stakeButton, unstakeButton].forEach {
      $0.snp.makeConstraints {
        $0.height.equalTo(50)
        $0.left.right.equalToSuperview().inset(15)
        $0.bottom.equalToSuperview().offset(-40)
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
      .map { $0.coinSettings?.txFee }
    
    Driver.combineLatest(coinBalanceDriver, stakeDetailsDriver)
      .drive(onNext: { [headerView] coinBalance, stakeDetails in
        let amountView = CryptoFiatAmountView()
        amountView.configure(for: coinBalance)
        
        headerView.removeAll()
        headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withDollarSign)
        headerView.add(title: localize(L.CoinDetails.balance), valueView: amountView)
        
        if stakeDetails.exist {
          headerView.add(title: localize(L.CoinStaking.Header.Amount.title),
                         value: "\(stakeDetails.amount ?? 0) \(coinBalance.type.code)")
          
          headerView.add(title: localize(L.CoinStaking.Header.Reward.title),
                         value: "\(stakeDetails.rewardAmount ?? 0) \(coinBalance.type.code), \(stakeDetails.rewardPercent ?? 0)%")
          
          headerView.add(title: localize(L.CoinStaking.Header.RewardAnnual.title),
                         value: "\(stakeDetails.rewardAnnualAmount ?? 0) \(coinBalance.type.code), \(stakeDetails.rewardAnnualPercent)%")
          
          headerView.add(title: localize(L.CoinStaking.Header.Duration.title),
                         value: String(format: localize(L.CoinStaking.Header.Duration.value), stakeDetails.days ?? 0))
        } else {
          headerView.add(title: localize(L.CoinStaking.Header.RewardAnnual.title),
                         value: "\(stakeDetails.rewardAnnualPercent)%")
        }
        
        headerView.add(title: localize(L.CoinStaking.Header.MinDuration.title),
                       value: String(format: localize(L.CoinStaking.Header.MinDuration.value), stakeDetails.minDays))
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
    
    stakeDetailsDriver
      .asObservable()
      .map { $0.exist }
      .bind(to: stakeButton.rx.isHidden)
      .disposed(by: disposeBag)
    
    stakeDetailsDriver
      .asObservable()
      .map { !$0.exist }
      .bind(to: unstakeButton.rx.isHidden)
      .disposed(by: disposeBag)
    
    stakeDetailsDriver
      .asObservable()
      .map { ($0.days ?? 0) >= $0.minDays }
      .bind(to: unstakeButton.rx.isEnabled)
      .disposed(by: disposeBag)
    
    Driver.merge(stakeButton.rx.tap.asDriver(),
                 unstakeButton.rx.tap.asDriver())
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let updateCoinAmountDriver = formView.rx.coinAmountText.asDriver(onErrorDriveWith: .empty())
    let maxDriver = formView.rx.maxTap
    let stakeDriver = stakeButton.rx.tap.asDriver()
    let unstakeDriver = unstakeButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinStakingPresenter.Input(updateCoinAmount: updateCoinAmountDriver,
                                                     max: maxDriver,
                                                     stake: stakeDriver,
                                                     unstake: unstakeDriver))
  }
}

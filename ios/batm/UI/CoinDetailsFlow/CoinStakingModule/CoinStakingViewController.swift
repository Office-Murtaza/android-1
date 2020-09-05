import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinStakingViewController: NavigationScreenViewController<CoinStakingPresenter> {
  
  let errorView = ErrorView()
  
  let headerView = HeaderView()
  
  let formView = CoinStakingFormView()
  
  let stakeButton = MDCButton.stake
  
  let unstakeButton = MDCButton.unstake
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    customView.rootScrollView.contentInsetAdjustmentBehavior = .never
    customView.contentView.addSubviews(errorView,
                                       headerView,
                                       formView,
                                       stakeButton,
                                       unstakeButton)
    
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
    [stakeButton, unstakeButton].forEach {
      $0.snp.makeConstraints {
        $0.height.equalTo(50)
        $0.top.equalTo(formView.snp.bottom).offset(5)
        $0.left.right.equalToSuperview().inset(15)
        $0.bottom.lessThanOrEqualToSuperview().offset(-30)
      }
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .drive(onNext: { [customView] in
        let title = String(format: localize(L.CoinStaking.title), $0)
        customView.setTitle(title)
      })
      .disposed(by: disposeBag)
    
    let coinBalanceDriver = presenter.state
      .map { $0.coinBalance }
      .filterNil()
    
    let stakeDetailsDriver = presenter.state
      .map { $0.stakeDetails }
      .filterNil()
    
    Driver.combineLatest(coinBalanceDriver, stakeDetailsDriver)
      .drive(onNext: { [headerView] coinBalance, stakeDetails in
        let amountView = CryptoFiatAmountView()
        amountView.configure(for: coinBalance)
        
        headerView.removeAll()
        headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withDollarSign)
        headerView.add(title: localize(L.CoinDetails.balance), valueView: amountView)
        
        if stakeDetails.exist {
          headerView.add(title: localize(L.CoinStaking.Header.Staked.title),
                         value: "\(stakeDetails.stakedAmount ?? 0) \(coinBalance.type.code)")
          
          headerView.add(title: localize(L.CoinStaking.Header.Rewards.title),
                         value: "\(stakeDetails.rewardsAmount ?? 0) \(coinBalance.type.code), \(stakeDetails.rewardsPercent ?? 0) %")
          
          headerView.add(title: localize(L.CoinStaking.Header.Duration.title),
                         value: String(format: localize(L.CoinStaking.Header.Duration.value), stakeDetails.stakedDays ?? 0))
          
          headerView.add(title: localize(L.CoinStaking.Header.MinDuration.title),
                         value: String(format: localize(L.CoinStaking.Header.MinDuration.value), stakeDetails.stakingMinDays ?? 0))
        }
      })
      .disposed(by: disposeBag)
    
    Driver.combineLatest(coinBalanceDriver, stakeDetailsDriver)
      .drive(onNext: { [formView] in formView.configure(with: $0.type.code, stakeDetails: $1) })
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
      .map { $0.unstakeAvailable }
      .bind(to: unstakeButton.rx.isEnabled)
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
    
    Driver.merge(stakeButton.rx.tap.asDriver(),
                 unstakeButton.rx.tap.asDriver())
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updateCoinAmountDriver = formView.rx.coinText.asDriver(onErrorDriveWith: .empty())
    let maxDriver = formView.rx.maxTap
    let stakeDriver = stakeButton.rx.tap.asDriver()
    let unstakeDriver = unstakeButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinStakingPresenter.Input(back: backDriver,
                                                     updateCoinAmount: updateCoinAmountDriver,
                                                     max: maxDriver,
                                                     stake: stakeDriver,
                                                     unstake: unstakeDriver))
  }
}

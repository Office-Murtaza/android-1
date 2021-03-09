import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinStakingViewController: ModuleViewController<CoinStakingPresenter> {
    let rootScrollView = RootScrollView()
    let formView = CoinStakingFormView()
    let withdrawView = WithdrawView()
    
    let stakingInfoView = CoinStakingInfoView()
    let cancelInfoView = CoinStakingInfoView()
    let rewardsInfoView = CoinStakingInfoView()
    
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
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        title = localize(L.CoinStaking.title)
        view.addSubview(rootScrollView)
        rootScrollView.contentView.addSubviews(formView,
                                               stakingInfoView,
                                               cancelInfoView,
                                               rewardsInfoView,
                                               buttonsStackView,
                                               withdrawView)
        
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
        formView.snp.makeConstraints {
            $0.top.equalToSuperview().offset(0)
            $0.height.equalTo(300)
            $0.left.right.equalToSuperview()
        }
        stakingInfoView.snp.makeConstraints {
            $0.top.equalTo(formView.stakingRateView.snp.bottom).offset(24)
            $0.left.right.equalToSuperview().offset(0)
            $0.height.greaterThanOrEqualTo(43)
        }
        cancelInfoView.snp.makeConstraints {
            $0.top.equalTo(stakingInfoView.snp.bottom).offset(24)
            $0.left.right.equalToSuperview().offset(0)
            $0.height.greaterThanOrEqualTo(43)
        }
        rewardsInfoView.snp.makeConstraints {
            $0.top.equalTo(cancelInfoView.snp.bottom).offset(24)
            $0.left.right.equalToSuperview().offset(0)
            $0.height.greaterThanOrEqualTo(43)
        }
        withdrawView.snp.makeConstraints {
            $0.left.right.equalToSuperview()
            $0.bottom.equalTo(buttonsStackView.snp.top).offset(-16)
            $0.height.equalTo(24)
        }
        buttonsStackView.snp.makeConstraints {
            $0.left.right.equalToSuperview().inset(15)
            $0.bottom.equalToSuperview().offset(-22)
        }
        buttons.forEach {
            $0.snp.makeConstraints {
                $0.height.equalTo(50)
            }
        }
    }
    
    func setupUIBindings() {
        let coinBalanceDriver = presenter.state
            .map { $0.coinBalance }
            .filterNil()
        
        let stakeDetailsDriver = presenter.state
            .map { $0.stakeDetails }
            .filterNil()
        
        let feeDriver = presenter.state
            .map { $0.coinDetails?.txFee }
        
        rx.firstTimeViewDidAppear
            .asObservable()
            .doOnNext { [weak self] in
                self?.presenter.didViewLoad.accept(())
            }
            .subscribe()
            .disposed(by: disposeBag)
        
        Driver.combineLatest(coinBalanceDriver, stakeDetailsDriver)
            .drive(onNext: { [weak self] coinBalance, stakeDetails in
                self?.setupDefaultInfoViews(with: stakeDetails)
                
                switch stakeDetails.status {
                case .notExist, .withdrawn:
                    self?.setupCreateView(with: stakeDetails, coinBalance: coinBalance)
                case .createPending, .created, .cancelPending:
                    self?.setupCancelView(with: stakeDetails, coinBalance: coinBalance)
                case .canceled, .withdrawPending:
                    self?.setupWithdrawView(with: stakeDetails, coinBalance: coinBalance)
                }
            })
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.coinAmount }
            .filterEmpty()
            .bind(to: formView.rx.fromCoinAmountText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { ($0.coinAmountError, $0.isEthLowBalance) }
            .subscribeOn(MainScheduler.instance)
            .subscribe { [weak self] result in
                guard let errorText = result.element?.0, let isEthLowBalance = result.element?.1  else { return }
                self?.formView.configure(from: errorText, isEthLowBalance: isEthLowBalance)
            }.disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.coin?.type }
            .filterNil()
            .bind(to: formView.rx.fromCoin)
            .disposed(by: disposeBag)
        
        presenter.state
            .drive(onNext: { [unowned self] state in
                guard let fromBalance = state.coinBalance, let fromDetails = state.coinDetails else { return }
                self.formView.configureBalance(for: fromBalance, coinDetails: fromDetails)
            })
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe { [weak self] (state) in
                let coinPrice = NSDecimalNumber(decimal: state.element?.coinBalance?.price ?? 0).stringValue.withDollarSign
                
                self?.formView.configureRateView(fromCoin: "1".coinFormatted.withCoinType(state.element?.coin?.type ?? .catm), toCurrency: coinPrice)
            }.disposed(by: disposeBag)
        
        Driver.combineLatest(coinBalanceDriver, stakeDetailsDriver, feeDriver)
            .drive(onNext: { [formView] in formView.configure(coinType: $0.type, stakeDetails: $1, fee: $2) })
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
            .map { !$0.shouldWithdrawButtonEnabled }
            .bind(to: withdrawButton.rx.isHidden)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { !$0.shouldShowWithdrawView }
            .bind(to: withdrawView.rx.isHidden)
            .disposed(by: disposeBag)
        
        Driver.merge(buttons.map { $0.rx.tap.asDriver() })
            .drive(onNext: { [view] in view?.endEditing(true) })
            .disposed(by: disposeBag)
        
        presenter.didUpdateCompleted
            .asDriver(onErrorDriveWith: .empty())
            .drive(onNext: { message in
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) { [weak self] in
                    guard let self = self else { return }
                    self.view.makeToast(message)
                }
            })
            .disposed(by: disposeBag)
    }
    
    override func setupBindings() {
        setupUIBindings()
        
        let updateCoinAmountDriver = formView.rx.fromCoinAmountText.asDriver(onErrorDriveWith: .empty())
        let maxDriver = formView.rx.maxFromTap
        let createDriver = createButton.rx.tap.asDriver()
        let cancelDriver = cancelButton.rx.tap.asDriver()
        let withdrawDriver = withdrawButton.rx.tap.asDriver()
        
        presenter.bind(input: CoinStakingPresenter.Input(updateCoinAmount: updateCoinAmountDriver,
                                                         max: maxDriver,
                                                         create: createDriver,
                                                         cancel: cancelDriver,
                                                         withdraw: withdrawDriver))
    }
    
    private func setupDefaultInfoViews(with stakeDetails: StakeDetails) {
        stakingInfoView.configureLeftView(with: localize(L.CoinStaking.Header.stakingAnualPercent),
                                          value: "\(stakeDetails.annualPercent ?? 0)%")
        cancelInfoView.configureLeftView(with: localize(L.CoinStaking.Header.cancelHoldPeriod),
                                         value: String(format: localize(L.CoinStaking.Header.CancelPeriod.value),
                                                       stakeDetails.holdPeriod ?? 0))
    }
    
    private func setupCreateView(with stakeDetails: StakeDetails, coinBalance: CoinBalance) {
        presenter.state
            .drive(onNext: { [stakeDetails, coinBalance, cancelInfoView, stakingInfoView] state in
                let annualReward = ((state.coinAmount.decimalValue ?? 0) * (Decimal(stakeDetails.annualPercent ?? 0))) / 100
                
                cancelInfoView.configureRightView(with: localize(L.CoinStaking.Header.annualRewardAmount),
                                                  value: "+\(annualReward) \(coinBalance.type.code)",
                                                  valueColor: .ceruleanBlue)
                stakingInfoView.configureRightView(with: localize(L.CoinStaking.Header.UsdConverted.title),
                                                   value: String(format: localize(L.CoinStaking.Header.UsdConverted.value),
                                                                 "\((state.coinAmount.decimalValue ?? 0) * coinBalance.price)"))
            })
            .disposed(by: disposeBag)
    }
    
    private func setupCancelView(with stakeDetails: StakeDetails, coinBalance: CoinBalance) {
        let rewardAmount = String(format: localize(L.CoinStaking.Header.Reward.value),
                                  "\(stakeDetails.rewardAmount?.formatted() ?? "0") \(coinBalance.type.code)",
                                  "\(stakeDetails.rewardPercent?.formatted() ?? "0")%")
        
        formView.configureStakeAmount(with: "\(stakeDetails.amount?.formatted() ?? "0")")
        
        stakingInfoView.configureRightView(with: localize(L.CoinStaking.Header.createdDate),
                                           value: stakeDetails.createTimestamp)
        
        cancelInfoView.configureRightView(with: localize(L.CoinStaking.Header.Duration.title),
                                          value: String(format: localize(L.CoinStaking.Header.Duration.value),
                                                        stakeDetails.duration ?? 0))
        
        rewardsInfoView.configureLeftView(with: localize(L.CoinStaking.Header.Reward.title),
                                          value: rewardAmount,
                                          valueColor: .ceruleanBlue)
        
        rewardsInfoView.isSeparatorHidden = true
    }
    
    private func setupWithdrawView(with stakeDetails: StakeDetails, coinBalance: CoinBalance) {
        let rewardAmount = String(format: localize(L.CoinStaking.Header.Reward.value),
                                  "\(stakeDetails.rewardAmount?.formatted() ?? "0") \(coinBalance.type.code)",
                                  "\(stakeDetails.rewardPercent?.formatted() ?? "0")%")
        formView.configureStakeAmount(with: "\(stakeDetails.amount?.formatted() ?? "0")")
        
        stakingInfoView.configureRightView(with: localize(L.CoinStaking.Header.createdDate),
                                           value: stakeDetails.createTimestamp)
        
        cancelInfoView.configureRightView(with: localize(L.CoinStaking.Header.CancelDate.title),
                                          value: stakeDetails.cancelTimestamp)
        
        rewardsInfoView.configureLeftView(with: localize(L.CoinStaking.Header.Reward.title),
                                          value: rewardAmount,
                                          valueColor: .ceruleanBlue)
        
        rewardsInfoView.configureRightView(with: localize(L.CoinStaking.Header.Duration.title),
                                           value: String(format: localize(L.CoinStaking.Header.Duration.value),
                                                         stakeDetails.duration ?? 0))
        
        withdrawView.configure(with: UIImage(named: "schedule"),
                               description: String(format: localize(L.CoinStaking.WithdrawView.description),
                                                   stakeDetails.tillWithdrawal ?? 0))
    }
}

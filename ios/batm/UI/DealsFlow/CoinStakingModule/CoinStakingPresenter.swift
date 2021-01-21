import Foundation
import RxSwift
import RxCocoa

final class CoinStakingPresenter: ModulePresenter, CoinStakingModule {
    typealias Store = ViewStore<CoinStakingAction, CoinStakingState>
    
    struct Input {
        var updateCoinAmount: Driver<String?>
        var max: Driver<Void>
        var create: Driver<Void>
        var cancel: Driver<Void>
        var withdraw: Driver<Void>
    }
    
    let didUpdateCompleted = PublishRelay<String>()
    private let reloadScreenRelay = PublishRelay<Void>()
    private let usecase: DealsUsecase
    private let store: Store
    private var stakeSuccessStatus: String?
    
    weak var delegate: CoinStakingModuleDelegate?
    
    var state: Driver<CoinStakingState> {
        return store.state
    }
    
    init(usecase: DealsUsecase,
         store: Store = CoinStakingStore()) {
        self.usecase = usecase
        self.store = store
    }
    
    func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails, stakeDetails: StakeDetails) {
        store.action.accept(.setupCoin(coin))
        store.action.accept(.setupCoinBalances(coinBalances))
        store.action.accept(.setupCoinDetails(coinDetails))
        store.action.accept(.setupStakeDetails(stakeDetails))
    }
    
    func bind(input: Input) {
        input.updateCoinAmount
            .asObservable()
            .map { CoinStakingAction.updateCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.max
            .asObservable()
            .withLatestFrom(state)
            .map { $0.maxValue.coinFormatted }
            .map { CoinStakingAction.updateCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        Driver.merge(input.create, input.cancel, input.withdraw)
            .asObservable()
            .doOnNext { [store] in store.action.accept(.updateValidationState) }
            .withLatestFrom(state)
            .filter { $0.validationState.isValid }
            .flatMap { [unowned self] in self.track(self.proceedWithStaking(for: $0)) }
            .subscribe(onNext: { [delegate] in delegate?.didFinishCoinStaking() })
            .disposed(by: disposeBag)
        
        reloadScreenRelay
            .flatMap { [unowned self] _ in
                return self.track(Completable.concat(self.usecase.getStakeDetails(for: .catm)
                                                        .do(onSuccess: { [store] in store.action.accept(.setupStakeDetails($0)) })
                                                        .asCompletable(),
                                                     self.usecase.getCoinDetails(for: .catm)
                                                        .do(onSuccess: { [store] in store.action.accept(.setupCoinDetails($0)) })
                                                        .asCompletable(),
                                                     self.usecase.getCoin(for: .catm)
                                                        .do(onSuccess: { [store] in store.action.accept(.setupCoin($0)) })
                                                        .asCompletable(),
                                                     self.usecase.getCoinsBalance()
                                                        .do(onSuccess: { [store] in store.action.accept(.setupCoinBalances($0.coins)) })
                                                        .asCompletable()))
            }
            .subscribe(onNext: { [weak self] in
                let toastMessage = String.localizedStringWithFormat(localize(L.CoinStaking.Toast.completed),
                                                                    self?.stakeSuccessStatus ?? "")
                self?.didUpdateCompleted.accept(toastMessage)
            })
            .disposed(by: disposeBag)
    }
    
    private func proceedWithStaking(for state: CoinStakingState) -> Completable {
        let coin = state.coin!
        let coinDetails = state.coinDetails!
        let coinAmount = state.coinAmount.decimalValue ?? 0.0
        let stakeDetails = state.stakeDetails!
        let stakeAction: Completable
        let stakeErrorStatus: String
        
        switch stakeDetails.status {
        case .notExist, .withdrawn:
            stakeAction = usecase.createStake(from: coin, with: coinDetails, amount: coinAmount)
            stakeErrorStatus = "creation"
            stakeSuccessStatus = "created"
        case .created:
            stakeAction = usecase.cancelStake(from: coin, with: coinDetails, stakeDetails: stakeDetails)
            stakeErrorStatus = "cancel"
            stakeSuccessStatus = "canceled"
        case .canceled:
            stakeAction = usecase.withdrawStake(from: coin, with: coinDetails, stakeDetails: stakeDetails)
            stakeErrorStatus = "withdraw"
            stakeSuccessStatus = "withdrawn"
        default:
            stakeAction = usecase.createStake(from: coin, with: coinDetails, amount: coinAmount)
            stakeErrorStatus = "creation"
            stakeSuccessStatus = "created"
        }
        
        return stakeAction
            .do(onError: { [weak self] error in
                let toastMessage = String.localizedStringWithFormat(localize(L.CoinStaking.Toast.error), stakeErrorStatus)
                self?.didUpdateCompleted.accept(toastMessage)
            }, onCompleted: { [weak self] in
                self?.reloadScreenRelay.accept(())
            })
            .catchError { [store] in
                if let apiError = $0 as? APIError, case let .serverError(error) = apiError, let code = error.code, code > 1 {
                    store.action.accept(.updateCoinAmountError(error.message))
                }
                throw $0
            }
    }
}

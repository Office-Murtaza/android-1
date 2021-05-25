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
    
    let didViewLoad = PublishRelay<Void>()
    let didUpdateCompleted = PublishRelay<String>()

    private var stakeSuccessStatus: String?
    private let reloadScreenRelay = PublishRelay<Void>()
    private let usecase: DealsUsecase
    private let store: Store
    
    weak var delegate: CoinStakingModuleDelegate?
    
    var state: Driver<CoinStakingState> {
        return store.state
    }
    
    init(usecase: DealsUsecase,
         store: Store = CoinStakingStore()) {
        self.usecase = usecase
        self.store = store
    }
    
    func setup() {}
    
    func bind(input: Input) {
        didViewLoad
            .flatMap { [unowned self] _ in
                return self.track(Observable.combineLatest(self.usecase.getStakeDetails(for: .catm)
                                                            .do(onSuccess: { [store] in
                                                                store.action.accept(.setupStakeDetails($0))
                                                            })
                                                            .asObservable(),
                                                           self.usecase.getCoin(for: .catm)
                                                            .do(onSuccess: { [store] in
                                                                store.action.accept(.setupCoin($0))
                                                            })
                                                            .asObservable(),
                                                           self.usecase.getCoinsBalance()
                                                            .do(onNext: { [store] in
                                                                store.action.accept(.setupCoinBalances($0.coins))
                                                            }),
                                                           self.usecase.getCoinDetails(for: .catm)
                                                            .do(onNext: { [store] in
                                                                store.action.accept(.setupCoinDetails($0))
                                                            })))
            }
            .subscribe()
            .disposed(by: disposeBag)
        
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
                                                        .do(onSuccess: { [store] in
                                                            store.action.accept(.setupStakeDetails($0))
                                                        })
                                                        .asCompletable(),
                                                     self.usecase.getCoin(for: .catm)
                                                        .do(onSuccess: { [store] in
                                                            store.action.accept(.setupCoin($0))
                                                        })
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
            stakeAction = usecase.createStake(from: coin, with: coinDetails, amount: coinAmount).asCompletable()
            stakeErrorStatus = localize(L.CoinStaking.ErrorStatus.creation)
            stakeSuccessStatus = localize(L.CoinStaking.SuccessStatus.created)
        case .created:
            stakeAction = usecase.cancelStake(from: coin, with: coinDetails, stakeDetails: stakeDetails).asCompletable()
            stakeErrorStatus = localize(L.CoinStaking.ErrorStatus.cancel)
            stakeSuccessStatus = localize(L.CoinStaking.SuccessStatus.canceled)
        case .canceled:
            stakeAction = usecase.withdrawStake(from: coin, with: coinDetails, stakeDetails: stakeDetails).asCompletable()
            stakeErrorStatus = localize(L.CoinStaking.ErrorStatus.withdraw)
            stakeSuccessStatus = localize(L.CoinStaking.SuccessStatus.withdrawn)
        default:
            stakeAction = usecase.createStake(from: coin, with: coinDetails, amount: coinAmount).asCompletable()
            stakeErrorStatus = localize(L.CoinStaking.ErrorStatus.creation)
            stakeSuccessStatus = localize(L.CoinStaking.SuccessStatus.created)
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

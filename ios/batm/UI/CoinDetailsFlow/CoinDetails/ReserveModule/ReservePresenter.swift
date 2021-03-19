import Foundation
import RxSwift
import RxCocoa

final class ReservePresenter: ModulePresenter, ReserveModule {
    typealias Store = ViewStore<ReserveAction, ReserveState>
    
    struct Input {
        var updateCoinAmount: Driver<String?>
        var max: Driver<Void>
        var reserve: Driver<Void>
    }
    
    weak var delegate: ReserveModuleDelegate?
    var state: Driver<ReserveState> { return store.state }
    
    let didViewLoadRelay = PublishRelay<Void>()
    
    private var coinType: CustomCoinType?
    private let usecase: CoinDetailsUsecase
    private let store: Store
    
    init(usecase: CoinDetailsUsecase,
         store: Store = ReserveStore()) {
        self.usecase = usecase
        self.store = store
    }
    
    func setup(with coinType: CustomCoinType) {
        self.coinType = coinType
    }
    
    func bind(input: Input) {
        didViewLoadRelay
            .flatMap { [unowned self] _ in
                return self.track(Observable.combineLatest(self.usecase.getCoinsBalance()
                                                            .do(onNext: { [weak self, store] in
                                                                store.action.accept(.setupCoinBalances($0.coins, self?.coinType ?? .bitcoin))
                                                            }),
                                                           self.usecase.getCoinDetails(for: self.coinType ?? .bitcoin)
                                                            .do(onNext: { [store] in
                                                                store.action.accept(.setupCoinDetails($0))
                                                            }),
                                                           self.usecase.getCoin(for: self.coinType ?? .bitcoin)
                                                            .do(onSuccess: { [store] in
                                                                store.action.accept(.setupCoin($0))
                                                            }).asObservable()),
                                  trackers: [self.errorTracker])
            }
            .subscribe()
            .disposed(by: disposeBag)
        
        input.updateCoinAmount
            .asObservable()
            .map { ReserveAction.updateCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.max
            .asObservable()
            .withLatestFrom(state)
            .map { $0.maxValue.coinFormatted }
            .map { ReserveAction.updateCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.reserve
            .asObservable()
            .doOnNext { [store] in store.action.accept(.updateValidationState) }
            .withLatestFrom(state)
            .filter { $0.validationState.isValid }
            .flatMap { [unowned self] in self.track(self.reserve(for: $0).asCompletable()) }
            .subscribe()
            .disposed(by: disposeBag)
    }
    
    private func reserve(for state: ReserveState) -> Single<TransactionDetails> {
        return usecase.reserve(from: state.coin!,
                               with: state.coinDetails!,
                               amount: state.coinAmount.decimalValue ?? 0.0)
            .do(onSuccess: { [weak self, store] details in
                self?.proceedReserve(with: L.CoinDetails.Success.transactionCreated,
                                     transactionDetails: details)
                return store.action.accept(.setupTransactionDetails(details))
            }, onError: { [weak self, store] in
                if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
                    if error.code == 2 {
                        self?.proceedReserve(with: L.CoinDetails.Error.transactionError, transactionDetails: state.transactionDetails)
                    } else {
                        store.action.accept(.makeInvalidState(error.message))
                    }
                }
                
                throw $0
            })
    }
    
    private func proceedReserve(with result: String, transactionDetails: TransactionDetails?) {
        let transactionResult = String(format: localize(result),
                                       localize(L.CoinDetails.reserve))
        delegate?.didFinishReserve(with: transactionResult, transactionDetails: transactionDetails)
    }
}

import Foundation
import RxSwift
import RxCocoa

final class RecallPresenter: ModulePresenter, RecallModule {
    
    typealias Store = ViewStore<RecallAction, RecallState>
    
    struct Input {
        var updateCoinAmount: Driver<String?>
        var max: Driver<Void>
        var recall: Driver<Void>
    }
    weak var delegate: RecallModuleDelegate?
    var state: Driver<RecallState> { return store.state }
    let didViewLoadRelay = PublishRelay<Void>()
    
    private var coinType: CustomCoinType?
    private let usecase: CoinDetailsUsecase
    private let store: Store
    
    init(usecase: CoinDetailsUsecase,
         store: Store = RecallStore()) {
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
            .map { RecallAction.updateCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.max
            .asObservable()
            .withLatestFrom(state)
            .map { $0.maxValue.coinFormatted }
            .map { RecallAction.updateCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.recall
            .asObservable()
            .doOnNext { [store] in store.action.accept(.updateValidationState) }
            .withLatestFrom(state)
            .filter { $0.validationState.isValid }
            .flatMap { [unowned self] in self.track(self.recall(for: $0)) }
            .subscribe()
            .disposed(by: disposeBag)
    }
    
    private func recall(for state: RecallState) -> Single<TransactionDetails> {
        return usecase.recall(from: state.coin!,
                              amount: state.coinAmount.decimalValue ?? 0.0)
            .do(onSuccess: { [weak self, store] details in
                self?.proceedRecall(with: L.CoinDetails.Success.transactionCreated,
                                    transactionDetails: details)
                return store.action.accept(.setupTransactionDetails(details))
            }, onError: { [weak self, store] in
                if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
                    if error.code == 2 {
                        self?.proceedRecall(with: L.CoinDetails.Error.transactionError,
                                            transactionDetails: state.transactionDetails)
                    } else {
                        store.action.accept(.makeInvalidState(error.message))
                    }
                }
                
                throw $0
            })
    }
    
    private func proceedRecall(with result: String, transactionDetails: TransactionDetails?) {
        let transactionResult = String(format: localize(result),
                                       localize(L.CoinDetails.recall))
        delegate?.didFinishRecall(with: transactionResult, transactionDetails: transactionDetails)
    }
}

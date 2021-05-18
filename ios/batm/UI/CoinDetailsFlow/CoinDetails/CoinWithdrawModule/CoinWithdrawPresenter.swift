import Foundation
import RxSwift
import RxCocoa

final class CoinWithdrawPresenter: ModulePresenter, CoinWithdrawModule {
    typealias Store = ViewStore<CoinWithdrawAction, CoinWithdrawState>
    
    struct Input {
        var updateAddress: Driver<String?>
        var updateCoinAmount: Driver<String?>
        var pasteAddress: Driver<Void>
        var max: Driver<Void>
        var submit: Driver<Void>
    }
    
    
    weak var delegate: CoinWithdrawModuleDelegate?
    var state: Driver<CoinWithdrawState> {
        return store.state
    }
    let didViewLoadRelay = PublishRelay<Void>()
    
    private var coinType: CustomCoinType?
    private let usecase: CoinDetailsUsecase
    private let store: Store
    
    init(usecase: CoinDetailsUsecase,
         store: Store = CoinWithdrawStore()) {
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
                                                            }).asObservable()))
            }
            .subscribe()
            .disposed(by: disposeBag)
        
        input.updateAddress
            .asObservable()
            .withLatestFrom(state) { ($0, $1) }
            .filter { $0 != $1.address }
            .map { $0.0 }
            .map { CoinWithdrawAction.updateAddress($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.updateCoinAmount
            .asObservable()
            .map { CoinWithdrawAction.updateCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.pasteAddress
            .asObservable()
            .map { UIPasteboard.general.string }
            .map { CoinWithdrawAction.updateAddress($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.max
            .asObservable()
            .withLatestFrom(state)
            .map { $0.maxValue.coinFormatted }
            .map { CoinWithdrawAction.updateCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.submit
            .asObservable()
            .doOnNext { [store] in store.action.accept(.updateValidationState) }
            .withLatestFrom(state)
            .filter { $0.validationState.isValid }
            .flatMap { [unowned self] in self.track(self.withdraw(for: $0)) }
            .subscribe()
            .disposed(by: disposeBag)
    }
    
    private func withdraw(for state: CoinWithdrawState) -> Single<TransactionDetails> {
        return usecase.withdraw(from: state.coin!,
                                with: state.coinDetails!,
                                to: state.address,
                                amount: state.coinAmount.decimalValue ?? 0.0)
            .do(onSuccess: { [weak self, store] details in
                self?.proceedWithdraw(with: L.CoinDetails.Success.transactionCreated,
                                      transactionDetails: details)
                return store.action.accept(.setupTransactionDetails(details))
            }, onError: { [weak self, store] in
                if let apiError = $0 as? APIError, case let .serverError(error) = apiError, let code = error.code, code > 1 {
                    if code == 2 {
                        self?.proceedWithdraw(with: L.CoinDetails.Error.transactionError,
                                              transactionDetails: state.transactionDetails)
                    } else if code == 3 {
                        store.action.accept(.updateCoinAmountError(error.message))
                    } else {
                        store.action.accept(.updateAddressError(error.message))
                    }
                }
                throw $0
            })
    }
    
    private func proceedWithdraw(with result: String, transactionDetails: TransactionDetails?) {
        let transactionResult = String(format: localize(result),
                                       localize(L.CoinDetails.withdraw))
        delegate?.didFinishCoinWithdraw(with: transactionResult, transactionDetails: transactionDetails)
    }

}

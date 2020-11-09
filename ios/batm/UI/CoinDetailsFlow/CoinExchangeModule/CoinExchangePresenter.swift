import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

final class CoinExchangePresenter: ModulePresenter, CoinExchangeModule {
    
    typealias Store = ViewStore<CoinExchangeAction, CoinExchangeState>
    
    struct Input {
        var updateFromCoinAmount: Driver<String?>
        var updatePickerItem: Driver<CustomCoinType>
        var toCoinType: Driver<CustomCoinType>
        var max: Driver<Void>
        var submit: Driver<Void>
    }
    
    private let usecase: CoinDetailsUsecase
    private let store: Store
    
    weak var delegate: CoinExchangeModuleDelegate?
    
    var state: Driver<CoinExchangeState> {
        return store.state
    }
    
    init(usecase: CoinDetailsUsecase,
         store: Store = CoinExchangeStore()) {
        self.usecase = usecase
        self.store = store
    }
    
    func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails) {
        store.action.accept(.setupCoin(coin))
        store.action.accept(.setupCoinBalances(coinBalances))
        store.action.accept(.setupCoinDetails(coinDetails))
    }
    
    func bind(input: Input) {
        input.updateFromCoinAmount
            .asObservable()
            .map { CoinExchangeAction.updateFromCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.toCoinType.drive(onNext: { [weak self] type in
            guard let self = self else { return }
            self.usecase.getCoinDetails(for: type).subscribe { [weak self] details in
                self?.store.action.accept(.updateToCoinTxFee(details.txFee))
            }
            .disposed(by: self.disposeBag)
        })
        .disposed(by: disposeBag)
        
        input.updatePickerItem
            .drive(onNext: { [store] in store.action.accept(.updateToCoinType($0)) })
            .disposed(by: disposeBag)
        
        input.max
            .asObservable()
            .withLatestFrom(state)
            .map { $0.maxValue.coinFormatted }
            .map { CoinExchangeAction.updateFromCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.submit
            .asObservable()
            .doOnNext { [store] in store.action.accept(.updateValidationState) }
            .withLatestFrom(state)
            .filter { $0.validationState.isValid }
            .flatMap { [unowned self] in self.track(self.exchange(for: $0)) }
            .subscribe(onNext: { [delegate] in delegate?.didFinishCoinExchange() })
            .disposed(by: disposeBag)
    }
    
    private func exchange(for state: CoinExchangeState) -> Completable {
        return usecase.exchange(from: state.fromCoin!,
                                with: state.coinDetails!,
                                to: state.toCoinType!,
                                amount: state.fromCoinAmount.decimalValue ?? 0.0,
                                toCoinAmount: state.toCoinAmount.decimalValue ?? 0.0)
            .catchError { [store] in
                if let apiError = $0 as? APIError, case let .serverError(error) = apiError, let code = error.code, code > 1 {
                    store.action.accept(.updateFromCoinAmountError(error.message))
                }
                
                throw $0
            }
    }
}

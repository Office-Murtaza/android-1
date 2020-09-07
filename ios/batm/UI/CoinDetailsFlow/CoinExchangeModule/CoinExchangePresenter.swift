import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

final class CoinExchangePresenter: ModulePresenter, CoinExchangeModule {
  
  typealias Store = ViewStore<CoinExchangeAction, CoinExchangeState>

  struct Input {
    var back: Driver<Void>
    var updateFromCoinAmount: Driver<String?>
    var updatePickerItem: Driver<CustomCoinType>
    var max: Driver<Void>
    var next: Driver<Void>
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
  
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings) {
    store.action.accept(.setupCoin(coin))
    store.action.accept(.setupCoinBalances(coinBalances))
    store.action.accept(.setupCoinSettings(coinSettings))
  }

  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishCoinExchange() })
      .disposed(by: disposeBag)
    
    input.updateFromCoinAmount
      .asObservable()
      .map { CoinExchangeAction.updateFromCoinAmount($0) }
      .bind(to: store.action)
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
    
    input.next
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
                                with: state.coinSettings!,
                                to: state.toCoinType!,
                                amount: state.fromCoinAmount.decimalValue ?? 0.0)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error.message))
        }
        
        throw $0
      }
  }
}

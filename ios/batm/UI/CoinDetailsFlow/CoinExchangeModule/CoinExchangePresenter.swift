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
    var updateCode: Driver<String?>
    var cancel: Driver<Void>
    var max: Driver<Void>
    var next: Driver<Void>
    var sendCode: Driver<Void>
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
    Driver.merge(input.back, input.cancel)
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
    
    input.updateCode
      .asObservable()
      .map { CoinExchangeAction.updateCode($0) }
      .bind(to: store.action)
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
      .flatMap { [unowned self] _ in self.track(self.requestCode()) }
      .subscribe(onNext: { [store] in store.action.accept(.showCodePopup) })
      .disposed(by: disposeBag)
    
    input.sendCode
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .flatMap { [unowned self] in self.track(self.exchange(for: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.didFinishCoinExchange() })
      .disposed(by: disposeBag)
  }
  
  private func requestCode() -> Completable {
    return usecase.requestCode()
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }
        
        throw $0
      }
  }
  
  private func exchange(for state: CoinExchangeState) -> Completable {
    return usecase.verifyCode(code: state.code)
      .andThen(usecase.exchange(from: state.fromCoin!,
                                with: state.coinSettings!,
                                to: state.toCoinType!,
                                amount: state.fromCoinAmount.doubleValue ?? 0.0))
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }
        
        throw $0
      }
  }
}

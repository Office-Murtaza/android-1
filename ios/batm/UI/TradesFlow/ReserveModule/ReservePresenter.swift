import Foundation
import RxSwift
import RxCocoa

final class ReservePresenter: ModulePresenter, ReserveModule {
  
  typealias Store = ViewStore<ReserveAction, ReserveState>

  struct Input {
    var back: Driver<Void>
    var updateCurrencyAmount: Driver<String?>
    var updateCoinAmount: Driver<String?>
    var updateCode: Driver<String?>
    var cancel: Driver<Void>
    var max: Driver<Void>
    var reserve: Driver<Void>
    var sendCode: Driver<Void>
  }
  
  private let usecase: CoinDetailsUsecase
  private let store: Store

  weak var delegate: ReserveModuleDelegate?
  
  var state: Driver<ReserveState> {
    return store.state
  }
  
  init(usecase: CoinDetailsUsecase,
       store: Store = ReserveStore()) {
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
      .drive(onNext: { [delegate] in delegate?.didFinishReserve() })
      .disposed(by: disposeBag)
    
    input.updateCurrencyAmount
      .asObservable()
      .map { ReserveAction.updateCurrencyAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCoinAmount
      .asObservable()
      .map { ReserveAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCode
      .asObservable()
      .map { ReserveAction.updateCode($0) }
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
      .flatMap { [unowned self] _ in self.track(self.requestCode()) }
      .subscribe(onNext: { [store] in store.action.accept(.showCodePopup) })
      .disposed(by: disposeBag)
    
    input.sendCode
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .flatMap { [unowned self] in self.track(self.reserve(for: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.didFinishReserve() })
      .disposed(by: disposeBag)
  }
  
  private func requestCode() -> Completable {
    return usecase.requestCode()
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error.message))
        }
        
        throw $0
      }
  }
  
  private func reserve(for state: ReserveState) -> Completable {
    return usecase.verifyCode(code: state.code)
      .andThen(usecase.reserve(from: state.coin!,
                               with: state.coinSettings!,
                               amount: state.coinAmount.doubleValue ?? 0.0))
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error.message))
        }
        
        throw $0
      }
  }
}

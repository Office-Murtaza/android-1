import Foundation
import RxSwift
import RxCocoa

final class RecallPresenter: ModulePresenter, RecallModule {
  
  typealias Store = ViewStore<RecallAction, RecallState>

  struct Input {
    var back: Driver<Void>
    var updateCurrencyAmount: Driver<String?>
    var updateCoinAmount: Driver<String?>
    var updateCode: Driver<String?>
    var cancel: Driver<Void>
    var max: Driver<Void>
    var recall: Driver<Void>
    var sendCode: Driver<Void>
  }
  
  private let usecase: CoinDetailsUsecase
  private let store: Store

  weak var delegate: RecallModuleDelegate?
  
  var state: Driver<RecallState> {
    return store.state
  }
  
  init(usecase: CoinDetailsUsecase,
       store: Store = RecallStore()) {
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
      .drive(onNext: { [delegate] in delegate?.didFinishRecall() })
      .disposed(by: disposeBag)
    
    input.updateCurrencyAmount
      .asObservable()
      .map { RecallAction.updateCurrencyAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCoinAmount
      .asObservable()
      .map { RecallAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCode
      .asObservable()
      .map { RecallAction.updateCode($0) }
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
      .flatMap { [unowned self] _ in self.track(self.requestCode()) }
      .subscribe(onNext: { [store] in store.action.accept(.showCodePopup) })
      .disposed(by: disposeBag)
    
    input.sendCode
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .flatMap { [unowned self] in self.track(self.recall(for: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.didFinishRecall() })
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
  
  private func recall(for state: RecallState) -> Completable {
    return usecase.verifyCode(code: state.code)
      .andThen(usecase.recall(from: state.coin!,
                              amount: state.coinAmount.doubleValue ?? 0.0))
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error.message))
        }
        
        throw $0
      }
  }
}

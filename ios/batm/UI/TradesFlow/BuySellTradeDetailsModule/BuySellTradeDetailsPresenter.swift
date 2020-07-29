import Foundation
import RxSwift
import RxCocoa

final class BuySellTradeDetailsPresenter: ModulePresenter, BuySellTradeDetailsModule {
  
  typealias Store = ViewStore<BuySellTradeDetailsAction, BuySellTradeDetailsState>

  struct Input {
    var back: Driver<Void>
    var updateCoinAmount: Driver<String?>
    var updateCurrencyAmount: Driver<String?>
    var max: Driver<Void>
    var sendRequest: Driver<Void>
  }
  
  private let store: Store
  private let usecase: TradesUsecase

  weak var delegate: BuySellTradeDetailsModuleDelegate?
  
  var state: Driver<BuySellTradeDetailsState> {
    return store.state
  }
  
  init(store: Store = BuySellTradeDetailsStore(),
       usecase: TradesUsecase) {
    self.store = store
    self.usecase = usecase
  }
  
  func setup(coinBalance: CoinBalance, trade: BuySellTrade, type: TradeType) {
    store.action.accept(.setupCoinBalance(coinBalance))
    store.action.accept(.setupTrade(trade))
    store.action.accept(.setupType(type))
  }

  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishBuySellTradeDetails() })
      .disposed(by: disposeBag)
    
    input.updateCoinAmount
      .asObservable()
      .map { BuySellTradeDetailsAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCurrencyAmount
      .asObservable()
      .map { BuySellTradeDetailsAction.updateCurrencyAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.max
      .asObservable()
      .withLatestFrom(state)
      .map { $0.maxValue.coinFormatted }
      .map { BuySellTradeDetailsAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.sendRequest
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { $0.data }
      .filterNil()
      .flatMap { [unowned self] in self.track(self.submitTradeRequest(for: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.didFinishBuySellTradeDetails() })
      .disposed(by: disposeBag)
  }
  
  private func submitTradeRequest(for data: SubmitTradeRequestData) -> Completable {
    return usecase.submitTradeRequest(for: data)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error.message))
        }
        
        throw $0
      }
  }
}

import Foundation
import RxSwift
import RxCocoa

struct SellDetailsForAnotherAddress: Equatable {
  let coin: BTMCoin
  let address: String
  let amount: Double
}

struct SellDetailsForCurrentAddress: Equatable {
  let coin: BTMCoin
}

final class CoinSellPresenter: ModulePresenter, CoinSellModule {
  
  typealias Store = ViewStore<CoinSellAction, CoinSellState>

  struct Input {
    var back: Driver<Void>
    var updateFromAnotherAddress: Driver<Bool>
    var updateCurrencyAmount: Driver<String?>
    var updateCode: Driver<String?>
    var cancel: Driver<Void>
    var max: Driver<Void>
    var next: Driver<Void>
    var sendCode: Driver<Void>
  }
  
  private let usecase: CoinDetailsUsecase
  private let store: Store

  weak var delegate: CoinSellModuleDelegate?
  
  var state: Driver<CoinSellState> {
    return store.state
  }
  
  init(usecase: CoinDetailsUsecase,
       store: Store = CoinSellStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func setup(coin: BTMCoin, coinBalance: CoinBalance, coinSettings: CoinSettings, details: SellDetails) {
    store.action.accept(.setupCoin(coin))
    store.action.accept(.setupCoinBalance(coinBalance))
    store.action.accept(.setupCoinSettings(coinSettings))
    store.action.accept(.setupDetails(details))
  }

  func bind(input: Input) {
    Driver.merge(input.back, input.cancel)
      .drive(onNext: { [delegate] in delegate?.didFinishCoinSell() })
      .disposed(by: disposeBag)
    
    input.updateFromAnotherAddress
      .asObservable()
      .map { CoinSellAction.updateFromAnotherAddress($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCurrencyAmount
      .asObservable()
      .map { CoinSellAction.updateCurrencyAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCode
      .asObservable()
      .map { CoinSellAction.updateCode($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.max
      .asObservable()
      .map { CoinSellAction.makeMaxCurrencyAmount }
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
      .flatMap { [unowned self] in self.track(self.presubmit(for: $0)) }
      .subscribe(onNext: { [store] in store.action.accept(.setupPreSubmitResponse($0)) })
      .disposed(by: disposeBag)
    
    setupBindings()
  }
  
  private func setupBindings() {
    state
      .asObservable()
      .distinctUntilChanged()
      .filter { $0.coin != nil }
      .filter { $0.presubmitResponse != nil }
      .filter { $0.fromAnotherAddress }
      .doOnNext { [store] _ in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { ($0.coin!, $0.presubmitResponse!) }
      .map { SellDetailsForAnotherAddress(coin: $0, address: $1.address, amount: $1.amount) }
      .subscribe(onNext: { [delegate] in delegate?.showSellDetailsForAnotherAddress($0) })
      .disposed(by: disposeBag)
    
    state
      .asObservable()
      .distinctUntilChanged()
      .filter { $0.coin != nil }
      .filter { $0.presubmitResponse != nil }
      .filter { !$0.fromAnotherAddress }
      .doOnNext { [store] _ in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .flatMap { [unowned self] in self.track(self.sell(for: $0).andThen(Observable.just($0))) }
      .map { SellDetailsForCurrentAddress(coin: $0.coin!) }
      .subscribe(onNext: { [delegate] in delegate?.showSellDetailsForCurrentAddress($0) })
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
  
  private func presubmit(for state: CoinSellState) -> Single<PreSubmitResponse> {
    let type = state.coin!.type
    let coinAmount = state.coinAmount.doubleValue ?? 0.0
    let currencyAmount = state.currencyAmount.doubleValue ?? 0
    
    return usecase.verifyCode(code: state.code)
      .andThen(usecase.presubmit(for: type, coinAmount: coinAmount, currencyAmount: currencyAmount))
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }
        
        throw $0
      }
  }
  
  private func sell(for state: CoinSellState) -> Completable {
    let coin = state.coin!
    let coinSettings = state.coinSettings!
    let amount = state.presubmitResponse!.amount
    let address = state.presubmitResponse!.address

    return usecase.sell(from: coin, with: coinSettings, amount: amount, to: address)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }

        throw $0
      }
  }

}

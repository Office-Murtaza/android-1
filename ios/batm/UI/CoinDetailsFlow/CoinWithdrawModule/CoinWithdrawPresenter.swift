import Foundation
import RxSwift
import RxCocoa

final class CoinWithdrawPresenter: ModulePresenter, CoinWithdrawModule {
  
  typealias Store = ViewStore<CoinWithdrawAction, CoinWithdrawState>

  struct Input {
    var back: Driver<Void>
    var updateAddress: Driver<String?>
    var updateCurrencyAmount: Driver<String?>
    var updateCoinAmount: Driver<String?>
    var pasteAddress: Driver<Void>
    var updateCode: Driver<String?>
    var cancel: Driver<Void>
    var max: Driver<Void>
    var next: Driver<Void>
    var sendCode: Driver<Void>
  }
  
  private let usecase: CoinDetailsUsecase
  private let store: Store

  weak var delegate: CoinWithdrawModuleDelegate?
  
  var state: Driver<CoinWithdrawState> {
    return store.state
  }
  
  init(usecase: CoinDetailsUsecase,
       store: Store = CoinWithdrawStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func setup(with coin: BTMCoin) {
    store.action.accept(.setupCoin(coin))
  }
  
  func setup(with coinBalance: CoinBalance) {
    store.action.accept(.setupCoinBalance(coinBalance))
  }

  func bind(input: Input) {
    Driver.merge(input.back, input.cancel)
      .drive(onNext: { [delegate] in delegate?.didFinishCoinWithdraw() })
      .disposed(by: disposeBag)
    
    input.updateAddress
      .asObservable()
      .map { CoinWithdrawAction.updateAddress($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCurrencyAmount
      .asObservable()
      .map { CoinWithdrawAction.updateCurrencyAmount($0) }
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
    
    input.updateCode
      .asObservable()
      .map { CoinWithdrawAction.updateCode($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.max
      .asObservable()
      .withLatestFrom(state)
      .map { String($0.maxValue) }
      .map { CoinWithdrawAction.updateCoinAmount($0) }
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
      .flatMap { [unowned self] in self.track(self.withdraw(for: $0))
      }
      .subscribe(onNext: { [delegate] in delegate?.didFinishCoinWithdraw() })
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
  
  private func withdraw(for state: CoinWithdrawState) -> Completable {
    return usecase.verifyCode(code: state.code)
      .andThen(usecase.withdraw(from: state.coin!,
                                to: state.address,
                                amount: Double(state.coinAmount) ?? 0.0))
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }
        
        throw $0
      }
  }
}
import Foundation
import RxSwift
import RxCocoa
import PhoneNumberKit

final class CoinSendGiftPresenter: ModulePresenter, CoinSendGiftModule {

  typealias Store = ViewStore<CoinSendGiftAction, CoinSendGiftState>

  struct Input {
    var back: Driver<Void>
    var updatePhone: Driver<String?>
    var updateCurrencyAmount: Driver<String?>
    var updateCoinAmount: Driver<String?>
    var pastePhone: Driver<Void>
    var updateCode: Driver<String?>
    var updateMessage: Driver<String?>
    var updateImageUrl: Driver<String?>
    var cancel: Driver<Void>
    var max: Driver<Void>
    var next: Driver<Void>
    var sendCode: Driver<Void>
  }
  
  private let usecase: CoinDetailsUsecase
  private let store: Store

  weak var delegate: CoinSendGiftModuleDelegate?
  
  var state: Driver<CoinSendGiftState> {
    return store.state
  }
  
  init(usecase: CoinDetailsUsecase,
       store: Store = CoinSendGiftStore()) {
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
      .drive(onNext: { [delegate] in delegate?.didFinishCoinSendGift() })
      .disposed(by: disposeBag)
    
    input.updatePhone
      .asObservable()
      .map { CoinSendGiftAction.updatePhone($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCurrencyAmount
      .asObservable()
      .map { CoinSendGiftAction.updateCurrencyAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCoinAmount
      .asObservable()
      .map { CoinSendGiftAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.pastePhone
      .asObservable()
      .map { UIPasteboard.general.string }
      .filterNil()
      .map { CoinSendGiftAction.pastePhone($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCode
      .asObservable()
      .map { CoinSendGiftAction.updateCode($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateMessage
      .asObservable()
      .map { CoinSendGiftAction.updateMessage($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateImageUrl
      .asObservable()
      .map { CoinSendGiftAction.updateImageUrl($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.max
      .asObservable()
      .withLatestFrom(state)
      .map { String($0.maxValue) }
      .map { CoinSendGiftAction.updateCoinAmount($0) }
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
      .flatMap { [unowned self] in self.track(self.sendGift(for: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.didFinishCoinSendGift() })
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
  
  private func sendGift(for state: CoinSendGiftState) -> Completable {
    return usecase.verifyCode(code: state.code)
      .andThen(usecase.sendGift(from: state.coin!,
                                to: state.formattedPhoneNumber,
                                amount: Double(state.coinAmount) ?? 0.0,
                                message: state.message,
                                imageUrl: state.imageUrl))
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }
        
        throw $0
      }
  }
}

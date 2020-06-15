import Foundation
import RxSwift
import RxCocoa
import FlagPhoneNumber

final class CoinSendGiftPresenter: ModulePresenter, CoinSendGiftModule {

  typealias Store = ViewStore<CoinSendGiftAction, CoinSendGiftState>

  struct Input {
    var back: Driver<Void>
    var updateCountry: Driver<FPNCountry>
    var updatePhone: Driver<String?>
    var updateCurrencyAmount: Driver<String?>
    var updateCoinAmount: Driver<String?>
    var updateMessage: Driver<String?>
    var updateImageId: Driver<String?>
    var pastePhone: Driver<Void>
    var max: Driver<Void>
    var next: Driver<Void>
    var updateCode: Driver<String?>
    var cancel: Driver<Void>
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
  
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings) {
    store.action.accept(.setupCoin(coin))
    store.action.accept(.setupCoinBalances(coinBalances))
    store.action.accept(.setupCoinSettings(coinSettings))
  }

  func bind(input: Input) {
    Driver.merge(input.back, input.cancel)
      .drive(onNext: { [delegate] in delegate?.didFinishCoinSendGift() })
      .disposed(by: disposeBag)
    
    input.updateCountry
      .asObservable()
      .map { CoinSendGiftAction.updateCountry($0) }
      .bind(to: store.action)
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
    
    input.updateImageId
      .asObservable()
      .map { CoinSendGiftAction.updateImageId($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.max
      .asObservable()
      .withLatestFrom(state)
      .map { $0.maxValue.coinFormatted }
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
                                with: state.coinSettings!,
                                to: state.phoneE164,
                                amount: state.coinAmount.doubleValue ?? 0.0,
                                message: state.message,
                                imageId: state.imageId))
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }
        
        throw $0
      }
  }
}

import Foundation
import RxSwift
import RxCocoa
import FlagPhoneNumber

final class CoinSendGiftPresenter: ModulePresenter, CoinSendGiftModule {

  typealias Store = ViewStore<CoinSendGiftAction, CoinSendGiftState>

  struct Input {
    var updatePhone: Driver<String?>
    var updateCoinAmount: Driver<String?>
    var updateMessage: Driver<String?>
    var updateImageId: Driver<String?>
    var max: Driver<Void>
    var submit: Driver<Void>
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
    input.updatePhone
      .asObservable()
      .map { CoinSendGiftAction.updatePhone($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCoinAmount
      .asObservable()
      .map { CoinSendGiftAction.updateCoinAmount($0) }
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
    
    input.submit
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .flatMap { [unowned self] in self.track(self.sendGift(for: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.didFinishCoinSendGift() })
      .disposed(by: disposeBag)
  }
  
  private func sendGift(for state: CoinSendGiftState) -> Completable {
    return usecase.sendGift(from: state.coin!,
                                with: state.coinSettings!,
                                to: state.phoneE164,
                                amount: state.coinAmount.doubleValue ?? 0.0,
                                message: state.message,
                                imageId: state.imageId)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError, let code = error.code, code > 1 {
          store.action.accept(.updatePhoneError(error.message))
        }
        
        throw $0
      }
  }
}

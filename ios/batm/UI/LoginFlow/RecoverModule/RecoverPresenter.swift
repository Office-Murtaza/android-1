import Foundation
import RxSwift
import RxCocoa

class RecoverPresenter: ModulePresenter, RecoverModule {
  
  typealias Store = ViewStore<RecoverAction, RecoverState>
  
  struct Input {
    var updatePhone: Driver<ValidatablePhoneNumber>
    var updatePassword: Driver<String?>
    var updateCode: Driver<String?>
    var cancel: Driver<Void>
    var recoverWallet: Driver<Void>
    var confirmCode: Driver<Void>
  }
  
  let usecase: LoginUsecase
  let store: Store
  
  var state: Driver<RecoverState> {
    return store.state
  }
  
  weak var delegate: RecoverModuleDelegate?
  
  init(usecase: LoginUsecase,
       store: Store = RecoverStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func bind(input: Input) {
    input.updatePhone
      .asObservable()
      .map { RecoverAction.updatePhone($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updatePassword
      .asObservable()
      .map { RecoverAction.updatePassword($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCode
      .asObservable()
      .map { RecoverAction.updateCode($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.cancel
      .drive(onNext: { [delegate] in delegate?.didCancelRecovering() })
      .disposed(by: disposeBag)
    
    input.recoverWallet
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { ($0.validatablePhone.phoneE164, $0.password) }
      .flatMap { [unowned self] in self.track(self.recoverWallet(phoneNumber: $0.0, password: $0.1)) }
      .subscribe(onNext: { [store] in store.action.accept(.showCodePopup) })
      .disposed(by: disposeBag)
    
    input.confirmCode
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { $0.code }
      .flatMap { [unowned self] in self.track(self.verifyCode(code: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.finishRecovering() })
      .disposed(by: disposeBag)
  }
  
  private func recoverWallet(phoneNumber: String, password: String) -> Completable {
    return usecase.recoverWallet(phoneNumber: phoneNumber, password: password)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }
        
        throw $0
    }
  }
  
  private func verifyCode(code: String) -> Completable {
    return usecase.verifyCode(code: code)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }
        
        throw $0
    }
  }
  
}

import Foundation
import RxSwift
import RxCocoa

final class PhoneVerificationPresenter: ModulePresenter, PhoneVerificationModule {
  
  typealias Store = ViewStore<PhoneVerificationAction, PhoneVerificationState>

  struct Input {
    var code: Driver<String>
    var next: Driver<Void>
    var resendCode: Driver<Void>
  }
  
  let usecase: SettingsUsecase
  let store: Store
  let didTypeWrongCode = PublishRelay<Void>()
  let didSendNewCode = PublishRelay<Void>()
  
  var state: Driver<PhoneVerificationState> {
    return store.state
  }

  weak var delegate: PhoneVerificationModuleDelegate?
  
  init(usecase: SettingsUsecase,
       store: Store = PhoneVerificationStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func setup(phoneNumber: String, for mode: PhoneVerificationMode) {
    store.action.accept(.setupMode(mode))
    store.action.accept(.setupPhoneNumber(phoneNumber))
  }

  func bind(input: Input) {
    input.code
      .asObservable()
      .map { PhoneVerificationAction.updateCode($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.next
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .doOnNext { [unowned self] state in
        if !state.validationState.isValid {
          self.didTypeWrongCode.accept(())
        }
      }
      .filter { $0.validationState.isValid }
      .flatMap { [unowned self] state -> Driver<PhoneVerificationState> in
        switch state.mode {
        case .create: return .just(state)
        case .update: return self.track(self.update(for: state)).map { state }
        }
      }
      .subscribe(onNext: { [delegate] in delegate?.didFinishPhoneVerification(phoneNumber: $0.phoneNumber) })
      .disposed(by: disposeBag)
    
    input.resendCode
      .asObservable()
      .withLatestFrom(state)
      .flatMap { [unowned self] in self.track(self.verify(for: $0)) }
      .subscribe(onNext: { [unowned self] in
        self.store.action.accept(.updateCorrectCode($0.code))
        self.didSendNewCode.accept(())
      })
      .disposed(by: disposeBag)
    
    track(verify(for: store.currentState))
      .drive(onNext: { [store] in store.action.accept(.updateCorrectCode($0.code)) })
      .disposed(by: disposeBag)
  }
  
  private func verify(for state: PhoneVerificationState) -> Single<PhoneVerificationResponse> {
    return usecase.verifyAccount(phoneNumber: state.phoneNumber)
      .catchError { [unowned self] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError, let code = error.code, code > 1 {
          self.store.action.accept(.updateCodeError(error.message))
        }

        throw $0
      }
  }
  
  private func update(for state: PhoneVerificationState) -> Completable {
    return usecase.updatePhone(phoneNumber: state.phoneNumber)
      .catchError { [unowned self] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError, let code = error.code, code > 1 {
          self.store.action.accept(.updateCodeError(error.message))
        }

        throw $0
      }
  }
}

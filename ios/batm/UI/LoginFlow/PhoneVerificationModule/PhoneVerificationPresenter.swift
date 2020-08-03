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
  
  let usecase: LoginUsecase
  let store: Store
  let didTypeWrongCode = PublishRelay<Void>()
  let didSendNewCode = PublishRelay<Void>()
  
  var state: Driver<PhoneVerificationState> {
    return store.state
  }

  weak var delegate: PhoneVerificationModuleDelegate?
  
  init(usecase: LoginUsecase,
       store: Store = PhoneVerificationStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func setup(phoneNumber: String, password: String) {
    store.action.accept(.setupPhoneNumber(phoneNumber))
    store.action.accept(.setupPassword(password))
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
      .subscribe(onNext: { [delegate] in delegate?.didFinishPhoneVerification(phoneNumber: $0.phoneNumber,
                                                                              password: $0.password) })
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
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          self.store.action.accept(.makeInvalidState(error.message))
        }

        throw $0
      }
  }
}

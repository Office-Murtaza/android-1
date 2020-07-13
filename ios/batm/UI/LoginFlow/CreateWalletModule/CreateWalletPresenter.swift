import Foundation
import RxSwift
import RxCocoa

class CreateWalletPresenter: ModulePresenter, CreateWalletModule {
  
  typealias Store = ViewStore<CreateWalletAction, CreateWalletState>
  
  struct Input {
    var updatePhoneNumber: Driver<String?>
    var updatePassword: Driver<String?>
    var updateConfirmPassword: Driver<String?>
    var next: Driver<Void>
  }
  
  let usecase: LoginUsecase
  let store: Store
  
  var state: Driver<CreateWalletState> {
    return store.state
  }
  
  weak var delegate: CreateWalletModuleDelegate?
  
  init(usecase: LoginUsecase,
       store: Store = CreateWalletStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func bind(input: Input) {
    input.updatePhoneNumber
      .asObservable()
      .map { CreateWalletAction.updatePhoneNumber($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updatePassword
      .asObservable()
      .map { CreateWalletAction.updatePassword($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateConfirmPassword
      .asObservable()
      .map { CreateWalletAction.updateConfirmPassword($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.next
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { ($0.phoneE164, $0.password) }
      .flatMap { [unowned self] in self.track(self.checkAndVerify(phoneNumber: $0.0, password: $0.1)) }
      .subscribe(onNext: { [delegate] _ in delegate?.finishCreatingWallet() })
      .disposed(by: disposeBag)
  }
  
  private func checkAndVerify(phoneNumber: String, password: String) -> Single<PhoneVerificationResponse> {
    return usecase.checkAndVerifyCreatingAccount(phoneNumber: phoneNumber, password: password)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }

        throw $0
      }
  }
  
}

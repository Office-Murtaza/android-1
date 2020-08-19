import Foundation
import RxSwift
import RxCocoa

final class UpdatePhonePresenter: ModulePresenter, UpdatePhoneModule {
  
  typealias Store = ViewStore<UpdatePhoneAction, UpdatePhoneState>

  struct Input {
    var updatePhone: Driver<ValidatablePhoneNumber>
    var next: Driver<Void>
  }
  
  private let usecase: SettingsUsecase
  private let store: Store

  weak var delegate: UpdatePhoneModuleDelegate?
  
  var state: Driver<UpdatePhoneState> {
    return store.state
  }
  
  init(usecase: SettingsUsecase,
       store: Store = UpdatePhoneStore()) {
    self.usecase = usecase
    self.store = store
  }

  func bind(input: Input) {
    input.updatePhone
      .asObservable()
      .map { UpdatePhoneAction.updatePhone($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.next
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { $0.validatablePhone.phoneE164 }
      .flatMap { [unowned self] in self.track(self.updatePhone(phoneNumber: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.didUpdatePhone() })
      .disposed(by: disposeBag)
  }
  
  private func updatePhone(phoneNumber: String) -> Completable {
    return usecase.updatePhone(phoneNumber: phoneNumber)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error.message))
        }
        
        throw $0
      }
  }
}

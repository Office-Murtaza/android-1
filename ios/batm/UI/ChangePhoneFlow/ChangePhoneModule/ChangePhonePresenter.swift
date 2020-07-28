import Foundation
import RxSwift
import RxCocoa

final class ChangePhonePresenter: ModulePresenter, ChangePhoneModule {
  
  typealias Store = ViewStore<ChangePhoneAction, ChangePhoneState>

  struct Input {
    var back: Driver<Void>
    var updatePhone: Driver<ValidatablePhoneNumber>
    var cancel: Driver<Void>
    var changePhone: Driver<Void>
  }
  
  private let usecase: SettingsUsecase
  private let store: Store

  weak var delegate: ChangePhoneModuleDelegate?
  
  var state: Driver<ChangePhoneState> {
    return store.state
  }
  
  init(usecase: SettingsUsecase,
       store: Store = ChangePhoneStore()) {
    self.usecase = usecase
    self.store = store
  }

  func bind(input: Input) {
    Driver.merge(input.back, input.cancel)
      .drive(onNext: { [delegate] in delegate?.didFinishChangePhone() })
      .disposed(by: disposeBag)
    
    input.updatePhone
      .asObservable()
      .map { ChangePhoneAction.updatePhone($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.changePhone
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { $0.validatablePhone.phoneE164 }
      .flatMap { [unowned self] in self.track(self.changePhone(phoneNumber: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.didChangePhone() })
      .disposed(by: disposeBag)
  }
  
  private func changePhone(phoneNumber: String) -> Completable {
    return usecase.changePhone(phoneNumber: phoneNumber)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error.message))
        }
        
        throw $0
      }
  }
}

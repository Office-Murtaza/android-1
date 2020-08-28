import Foundation
import RxSwift
import RxCocoa

final class UpdatePasswordPresenter: ModulePresenter, UpdatePasswordModule {
  
  typealias Store = ViewStore<UpdatePasswordAction, UpdatePasswordState>

  struct Input {
    var updateOldPassword: Driver<String?>
    var updateNewPassword: Driver<String?>
    var updateConfirmNewPassword: Driver<String?>
    var update: Driver<Void>
  }
  
  private let usecase: SettingsUsecase
  private let store: Store

  weak var delegate: UpdatePasswordModuleDelegate?
  
  var state: Driver<UpdatePasswordState> {
    return store.state
  }
  
  init(usecase: SettingsUsecase,
       store: Store = UpdatePasswordStore()) {
    self.usecase = usecase
    self.store = store
  }

  func bind(input: Input) {
    input.updateOldPassword
      .asObservable()
      .map { UpdatePasswordAction.updateOldPassword($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateNewPassword
      .asObservable()
      .map { UpdatePasswordAction.updateNewPassword($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateConfirmNewPassword
      .asObservable()
      .map { UpdatePasswordAction.updateConfirmNewPassword($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.update
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { ($0.oldPassword, $0.newPassword) }
      .flatMap { [unowned self] in self.track(self.updatePassword(oldPassword: $0, newPassword: $1)) }
      .subscribe(onNext: { [delegate] in delegate?.didUpdatePassword() })
      .disposed(by: disposeBag)
  }
  
  private func updatePassword(oldPassword: String, newPassword: String) -> Completable {
    return usecase.updatePassword(oldPassword: oldPassword, newPassword: newPassword)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError, let code = error.code {
          if code == 2 {
            store.action.accept(.updateOldPasswordError(error.message))
          }
          if code == 3 {
            store.action.accept(.updateNewPasswordError(error.message))
          }
        }
        
        throw $0
    }
  }
}

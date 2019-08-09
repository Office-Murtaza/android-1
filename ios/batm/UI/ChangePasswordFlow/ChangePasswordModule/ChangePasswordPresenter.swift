import Foundation
import RxSwift
import RxCocoa

final class ChangePasswordPresenter: ModulePresenter, ChangePasswordModule {
  
  typealias Store = ViewStore<ChangePasswordAction, ChangePasswordState>

  struct Input {
    var back: Driver<Void>
    var updateOldPassword: Driver<String?>
    var updateNewPassword: Driver<String?>
    var updateConfirmNewPassword: Driver<String?>
    var cancel: Driver<Void>
    var changePassword: Driver<Void>
  }
  
  private let usecase: SettingsUsecase
  private let store: Store

  weak var delegate: ChangePasswordModuleDelegate?
  
  var state: Driver<ChangePasswordState> {
    return store.state
  }
  
  init(usecase: SettingsUsecase,
       store: Store = ChangePasswordStore()) {
    self.usecase = usecase
    self.store = store
  }

  func bind(input: Input) {
    Driver.merge(input.back, input.cancel)
      .drive(onNext: { [delegate] in delegate?.didFinishChangePassword() })
      .disposed(by: disposeBag)
    
    input.updateOldPassword
      .asObservable()
      .map { ChangePasswordAction.updateOldPassword($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateNewPassword
      .asObservable()
      .map { ChangePasswordAction.updateNewPassword($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateConfirmNewPassword
      .asObservable()
      .map { ChangePasswordAction.updateConfirmNewPassword($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.changePassword
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { ($0.oldPassword, $0.newPassword) }
      .flatMap { [unowned self] in self.track(self.changePassword(oldPassword: $0, newPassword: $1)) }
      .subscribe(onNext: { [delegate] in delegate?.didChangePassword() })
      .disposed(by: disposeBag)
  }
  
  private func changePassword(oldPassword: String, newPassword: String) -> Completable {
    return usecase.changePassword(oldPassword: oldPassword, newPassword: newPassword)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }
        
        throw $0
    }
  }
}

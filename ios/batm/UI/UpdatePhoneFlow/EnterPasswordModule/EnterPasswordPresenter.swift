import Foundation
import RxSwift
import RxCocoa

final class EnterPasswordPresenter: ModulePresenter, EnterPasswordModule {
  
  typealias Store = ViewStore<EnterPasswordAction, EnterPasswordState>

  struct Input {
    var updatePassword: Driver<String?>
    var verifyPassword: Driver<Void>
  }
  
  private let usecase: SettingsUsecase
  private let store: Store

  weak var delegate: EnterPasswordModuleDelegate?
  
  var state: Driver<EnterPasswordState> {
    return store.state
  }
  
  init(usecase: SettingsUsecase,
       store: Store = EnterPasswordStore()) {
    self.usecase = usecase
    self.store = store
  }

  func bind(input: Input) {
    input.updatePassword
      .asObservable()
      .map { EnterPasswordAction.updatePassword($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.verifyPassword
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { $0.password }
      .flatMap { [unowned self] in self.track(self.verifyPassword(password: $0)) }
      .subscribe()
      .disposed(by: disposeBag)
  }
  
  private func verifyPassword(password: String) -> Completable {
    return usecase.verifyPassword(password: password)
      .do(onSuccess: { [delegate, store] matched in
        if matched {
          delegate?.didMatchPassword()
        } else {
          let error = localize(L.EnterPassword.Form.Error.wrongPassword)
          store.action.accept(.updatePasswordError(error))
        }
      })
      .asCompletable()
  }
}

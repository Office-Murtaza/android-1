import Foundation
import RxSwift
import RxCocoa

final class EnterPasswordPresenter: ModulePresenter, EnterPasswordModule {
  
  typealias Store = ViewStore<EnterPasswordAction, EnterPasswordState>

  struct Input {
    var back: Driver<Void>
    var updatePassword: Driver<String?>
    var cancel: Driver<Void>
    var checkPassword: Driver<Void>
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
    Driver.merge(input.back, input.cancel)
      .drive(onNext: { [delegate] in delegate?.didFinishEnterPassword() })
      .disposed(by: disposeBag)
    
    input.updatePassword
      .asObservable()
      .map { EnterPasswordAction.updatePassword($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.checkPassword
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { $0.password }
      .flatMap { [unowned self] in self.track(self.checkPassword(password: $0)) }
      .subscribe()
      .disposed(by: disposeBag)
  }
  
  private func checkPassword(password: String) -> Completable {
    return usecase.checkPassword(password: password)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error.message))
        }
        
        throw $0
      }
      .do(onSuccess: { [delegate, store] matched in
        if matched {
          delegate?.didMatchPassword()
        } else {
          let error = localize(L.EnterPassword.Form.Error.wrongPassword)
          store.action.accept(.makeInvalidState(error))
        }
      })
      .asCompletable()
  }
}

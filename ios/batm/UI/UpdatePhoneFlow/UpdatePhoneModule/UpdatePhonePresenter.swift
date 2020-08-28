import Foundation
import RxSwift
import RxCocoa

final class UpdatePhonePresenter: ModulePresenter, UpdatePhoneModule {
  
  typealias Store = ViewStore<UpdatePhoneAction, UpdatePhoneState>
  
  struct Input {
    var updatePhoneNumber: Driver<String?>
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
  
  func setup(oldPhoneNumber: String) {
    store.action.accept(.setupOldPhoneNumber(oldPhoneNumber))
  }
  
  func bind(input: Input) {
    input.updatePhoneNumber
      .asObservable()
      .map { UpdatePhoneAction.updatePhoneNumber($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.next
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { $0.phoneE164 }
      .flatMap { [unowned self] in self.track(self.updatePhone(phoneNumber: $0)) }
      .subscribe()
      .disposed(by: disposeBag)
  }
  
  private func updatePhone(phoneNumber: String) -> Completable {
    return usecase.verifyPhone(phoneNumber: phoneNumber)
      .do(onSuccess: { [delegate, store] matched in
        if matched {
          let error = localize(L.UpdatePhone.Form.Error.phoneUsed)
          store.action.accept(.updatePhoneNumberError(error))
        } else {
          delegate?.didNotMatchNewPhoneNumber(phoneNumber)
        }
      })
      .asCompletable()
  }
}

import Foundation
import RxSwift
import RxCocoa

final class ChangePinPresenter: ModulePresenter, ChangePinModule {
  
  typealias Store = ViewStore<ChangePinAction, ChangePinState>
  
  struct Input {
    var back: Driver<Void>
    var updateOldPin: Driver<String?>
    var updateNewPin: Driver<String?>
    var updateConfirmNewPin: Driver<String?>
    var cancel: Driver<Void>
    var changePin: Driver<Void>
  }
  
  private let usecase: SettingsUsecase
  private let store: Store
  
  weak var delegate: ChangePinModuleDelegate?
  
  var state: Driver<ChangePinState> {
    return store.state
  }
  
  init(usecase: SettingsUsecase,
       store: Store = ChangePinStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func bind(input: Input) {
    Driver.merge(input.back, input.cancel)
      .drive(onNext: { [delegate] in delegate?.didFinishChangePin() })
      .disposed(by: disposeBag)
    
    input.updateOldPin
      .asObservable()
      .map { ChangePinAction.updateOldPin($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateNewPin
      .asObservable()
      .map { ChangePinAction.updateNewPin($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateConfirmNewPin
      .asObservable()
      .map { ChangePinAction.updateConfirmNewPin($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.changePin
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { ($0.oldPin, $0.newPin) }
      .flatMap { [unowned self] in self.track(self.changePin(oldPin: $0, newPin: $1)) }
      .subscribe(onNext: { [delegate] in delegate?.didFinishChangePin() })
      .disposed(by: disposeBag)
  }
  
  private func changePin(oldPin: String, newPin: String) -> Completable {
    return usecase.changePin(oldPin: oldPin, newPin: newPin)
      .catchError { [store] in
        if let error = $0 as? PinCodeError, case .notMatch = error {
          store.action.accept(.makeInvalidState(localize(L.ChangePin.Form.Error.notMatch)))
        }
        
        throw $0
    }
  }
}

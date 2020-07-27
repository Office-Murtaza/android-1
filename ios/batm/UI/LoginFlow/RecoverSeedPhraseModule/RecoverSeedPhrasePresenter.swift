import Foundation
import RxSwift
import RxCocoa

class RecoverSeedPhrasePresenter: ModulePresenter, RecoverSeedPhraseModule {
  typealias Store = ViewStore<RecoverSeedPhraseAction, RecoverSeedPhraseState>
  
  struct Input {
    var updateSeedPhrase: Driver<[String]>
    var next: Driver<Void>
  }
  
  private let usecase: LoginUsecase
  private let store: Store
  
  weak var delegate: RecoverSeedPhraseModuleDelegate?
  
  var state: Driver<RecoverSeedPhraseState> {
    return store.state
  }
  
  init(usecase: LoginUsecase,
       store: Store = RecoverSeedPhraseStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func setup(phoneNumber: String, password: String) {
    store.action.accept(.setupPhoneNumber(phoneNumber))
    store.action.accept(.setupPassword(password))
  }
  
  func bind(input: Input) {
    input.updateSeedPhrase
      .asObservable()
      .map { RecoverSeedPhraseAction.updateSeedPhrase($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.next
      .asObservable()
      .doOnNext { [store] _ in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .flatMap { [unowned self] in self.track(self.recoverWallet(for: $0)) }
      .subscribe(onNext: { [delegate] _ in delegate?.finishRecoveringSeedPhrase() })
      .disposed(by: disposeBag)
  }
  
  private func recoverWallet(for state: RecoverSeedPhraseState) -> Completable {
    return usecase.recoverWallet(phoneNumber: state.phoneNumber,
                                 password: state.password,
                                 seedPhrase: state.fullSeedPhrase)
      .catchError { [store, delegate] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          if error.code == 1 {
            delegate?.cancelRecoveringSeedPhrase()
          } else {
            store.action.accept(.makeInvalidState(error.message))
          }
        }

        throw $0
      }
  }
}

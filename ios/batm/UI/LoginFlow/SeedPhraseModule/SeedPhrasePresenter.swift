import Foundation
import RxSwift
import RxCocoa

class SeedPhrasePresenter: ModulePresenter, SeedPhraseModule {
  typealias Store = ViewStore<SeedPhraseAction, SeedPhraseState>
  
  struct Input {
    var copy: Driver<Void>
    var next: Driver<Void>
  }
  
  private let usecase: LoginUsecase
  private let store: Store
  
  weak var delegate: SeedPhraseModuleDelegate?
  
  var state: Driver<SeedPhraseState> {
    return store.state
  }
  
  init(usecase: LoginUsecase,
       store: Store = SeedPhraseStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func setup(phoneNumber: String, password: String) {
    store.action.accept(.setupPhoneNumber(phoneNumber))
    store.action.accept(.setupPassword(password))
  }
  
  func bind(input: Input) {
    input.copy
      .withLatestFrom(state)
      .drive(onNext: { UIPasteboard.general.string = $0.seedPhrase })
      .disposed(by: disposeBag)
    
    input.next
      .asObservable()
      .doOnNext { [store] _ in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .flatMap { [unowned self] in self.track(self.createAccount(for: $0)) }
      .subscribe(onNext: { [delegate] _ in delegate?.finishCopyingSeedPhrase() })
      .disposed(by: disposeBag)
    
    setupBindings()
  }
  
  private func setupBindings() {
    self.track(usecase.createWallet().andThen(usecase.getSeedPhrase()))
      .asObservable()
      .map { SeedPhraseAction.setupSeedPhrase($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
  }
  
  private func createAccount(for state: SeedPhraseState) -> Completable {
    return usecase.createAccount(phoneNumber: state.phoneNumber, password: state.password)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }
        
        throw $0
    }
  }
}

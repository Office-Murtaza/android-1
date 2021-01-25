import Foundation
import RxSwift
import RxCocoa

class SeedPhrasePresenter: ModulePresenter, SeedPhraseModule {
  typealias Store = ViewStore<SeedPhraseAction, SeedPhraseState>
  
  struct Input {
    var copy: Driver<Void>
    var next: Driver<Void>
    var generate: Driver<Void>
    var updateSeedPhrase: Driver<[String]>
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
  
  func setup(for mode: SeedPhraseMode) {
    store.action.accept(.setupMode(mode))
  }
  
  func bind(input: Input) {
    input.copy
      .withLatestFrom(state)
      .drive(onNext: { UIPasteboard.general.string = $0.fullSeedPhrase })
      .disposed(by: disposeBag)
    
    input.updateSeedPhrase
      .asObservable()
      .map { SeedPhraseAction.updateSeedPhrase($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.next
      .asObservable()
      .doOnNext { [store] _ in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .flatMap { [unowned self] state -> Driver<Void> in
        switch state.mode {
        case let .creation(phoneNumber, password):
          return self.track(self.createAccount(phoneNumber: phoneNumber, password: password))
        case .showing:
          return .just(())
        }
      }
      .subscribe(onNext: { [delegate] in delegate?.didFinishCopyingSeedPhrase() })
      .disposed(by: disposeBag)
    
    input.generate
     .asObservable()
     .withLatestFrom(state)
     .map { $0.mode }
     .flatMap { [unowned self] mode -> Driver<String> in
       switch mode {
       case .creation:
        return self.track(self.usecase.createWallet(seedPhrase: store.currentState.fullSeedPhrase).andThen(self.usecase.getSeedPhrase()))
       case .showing:
         return self.track(self.usecase.getSeedPhrase())
       }
   }
        .map { SeedPhraseAction.generateSeedPhrase($0.separatedWords) }
   .bind(to: store.action)
   .disposed(by: disposeBag)
    
    setupBindings()
  }
  
  private func setupBindings() {
//    Observable.just(())

  }
  
  private func createAccount(phoneNumber: String, password: String) -> Completable {
    return usecase.createAccount(phoneNumber: phoneNumber, password: password)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error.message))
        }
        
        throw $0
    }
  }
}

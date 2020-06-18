import Foundation
import RxSwift
import RxCocoa

class RecoverSeedPhrasePresenter: ModulePresenter, RecoverSeedPhraseModule {
  typealias Store = ViewStore<RecoverSeedPhraseAction, RecoverSeedPhraseState>
  
  struct Input {
    var paste: Driver<Void>
    var done: Driver<[String]>
  }
  
  private let usecase: LoginUsecase
  private let store: Store
  
  let seedPhraseWordsRelay = BehaviorRelay<[String]>(value: [])
  
  weak var delegate: RecoverSeedPhraseModuleDelegate?
  
  var state: Driver<RecoverSeedPhraseState> {
    return store.state
  }
  
  init(usecase: LoginUsecase,
       store: Store = RecoverSeedPhraseStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func bind(input: Input) {
    input.paste
      .map { UIPasteboard.general.string }
      .filterNil()
      .map { $0.split(separator: " ").map { String($0) } }
      .map { Array($0.prefix(BTMWallet.seedPhraseLength)) }
      .drive(onNext: { [seedPhraseWordsRelay] in seedPhraseWordsRelay.accept($0) })
      .disposed(by: disposeBag)
    input.done
      .filter { $0.count == BTMWallet.seedPhraseLength }
      .map { $0.map { $0.trimmingCharacters(in: .whitespaces) } }
      .map { $0.joined(separator: " ") }
      .asObservable()
      .doOnNext { [store] _ in store.action.accept(.updateValidationState) }
      .flatFilter(state.map { $0.validationState.isValid })
      .flatMap { [unowned self] in self.track(self.recoverWallet(seedPhrase: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.finishRecoveringSeedPhrase() })
      .disposed(by: disposeBag)
  }
  
  private func recoverWallet(seedPhrase: String) -> Completable {
    return usecase.recoverWallet(seedPhrase: seedPhrase)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }
        
        throw $0
      }
  }
}

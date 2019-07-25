import Foundation
import RxSwift
import RxCocoa

class RecoverSeedPhrasePresenter: ModulePresenter, RecoverSeedPhraseModule {
  struct Input {
    var paste: Driver<Void>
    var done: Driver<[String]>
  }
  
  private let usecase: LoginUsecase
  
  let seedPhraseRelay = BehaviorRelay<String?>(value: nil)
  
  weak var delegate: RecoverSeedPhraseModuleDelegate?
  
  init(usecase: LoginUsecase) {
    self.usecase = usecase
  }
  
  func bind(input: Input) {
    input.paste
      .drive(onNext: { [seedPhraseRelay] in seedPhraseRelay.accept(UIPasteboard.general.string) })
      .disposed(by: disposeBag)
    input.done
      .filter { $0.count == 12 }
      .map { $0.map { $0.trimmingCharacters(in: .whitespaces) } }
      .map { $0.joined(separator: " ") }
      .asObservable()
      .flatMap { [unowned self] in self.track(self.usecase.recoverWallet(seedPhrase: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.finishRecoveringSeedPhrase() })
      .disposed(by: disposeBag)
  }
}

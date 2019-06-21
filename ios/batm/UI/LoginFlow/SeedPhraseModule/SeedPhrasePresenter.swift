import Foundation
import RxSwift
import RxCocoa

class SeedPhrasePresenter: ModulePresenter, SeedPhraseModule {
  struct Input {
    var copy: Driver<Void>
    var done: Driver<Void>
  }
  
  let usecase: LoginUsecase
  let seedPhraseRelay = BehaviorRelay<String?>(value: nil)
  
  weak var delegate: SeedPhraseModuleDelegate?
  
  init(usecase: LoginUsecase) {
    self.usecase = usecase
    
    super.init()
    
    usecase.getSeedPhrase()
      .asObservable()
      .bind(to: seedPhraseRelay)
      .disposed(by: disposeBag)
  }
  
  func bind(input: Input) {
    input.copy
      .drive(onNext: { [seedPhraseRelay] in UIPasteboard.general.string = seedPhraseRelay.value })
      .disposed(by: disposeBag)
    input.done
      .drive(onNext: { [delegate] in delegate?.finishCopyingSeedPhrase() })
      .disposed(by: disposeBag)
  }
}

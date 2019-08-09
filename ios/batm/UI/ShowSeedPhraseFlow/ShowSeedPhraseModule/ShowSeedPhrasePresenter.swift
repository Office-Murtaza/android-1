import Foundation
import RxSwift
import RxCocoa

final class ShowSeedPhrasePresenter: ModulePresenter, ShowSeedPhraseModule {

  struct Input {
    var back: Driver<Void>
    var copy: Driver<Void>
    var done: Driver<Void>
  }
  
  private let usecase: LoginUsecase
  let seedPhraseRelay = BehaviorRelay<String?>(value: nil)

  weak var delegate: ShowSeedPhraseModuleDelegate?
  
  init(usecase: LoginUsecase) {
    self.usecase = usecase
    
    super.init()
    
    self.track(usecase.getSeedPhrase())
      .asObservable()
      .bind(to: seedPhraseRelay)
      .disposed(by: disposeBag)
  }

  func bind(input: Input) {
    Driver.merge(input.back, input.done)
      .drive(onNext: { [delegate] in delegate?.didFinishShowSeedPhrase() })
      .disposed(by: disposeBag)
    
    input.copy
      .drive(onNext: { [seedPhraseRelay] in UIPasteboard.general.string = seedPhraseRelay.value })
      .disposed(by: disposeBag)
  }
}

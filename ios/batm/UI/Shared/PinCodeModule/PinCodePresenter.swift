import Foundation
import RxSwift
import RxCocoa

class PinCodePresenter: ModulePresenter, PinCodeModule {
  
  typealias Store = ViewStore<PinCodeAction, PinCodeState>
  
  struct Input {
    var addDigit: Driver<String>
    var removeDigit: Driver<Void>
  }
  
  private let usecase: PinCodeUsecase
  private let store: Store
  
  var state: Driver<PinCodeState> {
    return store.state
  }
  
  weak var delegate: PinCodeModuleDelegate?
  
  init(usecase: PinCodeUsecase,
       store: Store = PinCodeStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func setup(for stage: PinCodeStage) {
    store.action.accept(.updateStage(stage))
  }
  
  func bind(input: Input) {
    input.addDigit
      .asObservable()
      .map { PinCodeAction.addDigit($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.removeDigit
      .asObservable()
      .map { PinCodeAction.removeDigit }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    setupBindings()
  }
  
  private func setupBindings() {
    state
      .filter { $0.code.count == PinCodeDotsView.numberOfDots }
      .asObservable()
      .flatMap { [unowned self] state -> Driver<PinCodeState> in
        switch state.stage {
        case .setup: return self.track(self.usecase.save(pinCode: state.code)).map { state }
        case .confirmation, .verification:
          return self.track(self.usecase.verify(pinCode: state.code).andThen(self.usecase.refresh())).map { state }
        }
      }
      .subscribe(onNext: { [delegate] in delegate?.didFinishPinCode(for: $0.stage) })
      .disposed(by: disposeBag)
  }
  
}

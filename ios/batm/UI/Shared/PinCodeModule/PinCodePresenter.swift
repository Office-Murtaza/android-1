import Foundation
import RxSwift
import RxCocoa

class PinCodePresenter: ModulePresenter, PinCodeModule {
  
  typealias Store = ViewStore<PinCodeAction, PinCodeState>
  
  struct Input {
    var updateCode: Driver<String?>
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
    input.updateCode
      .asObservable()
      .distinctUntilChanged()
      .map { PinCodeAction.updateCode($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    setupBindings()
  }
  
  private func setupBindings() {
    state
      .filter { $0.code.count == PinCodeView.numberOfDots }
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

import Foundation
import RxSwift
import RxCocoa

class PinCodePresenter: ModulePresenter, PinCodeModule {
  
  typealias Store = ViewStore<PinCodeAction, PinCodeState>
  
  struct Input {
    var addDigit: Driver<String>
    var removeDigit: Driver<Void>
    var didDisappear: Driver<Void>
  }
  
  private let usecase: PinCodeUsecase
  private let store: Store
  
  let didTypeWrongPinCode = PublishRelay<Void>()
  
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
    store.action.accept(.setupStage(stage))
  }
  
  func setup(for type: PinCodeType) {
    store.action.accept(.setupType(type))
  }
  
  func setup(with correctCode: String) {
    store.action.accept(.setupCorrectCode(correctCode))
  }
  
  func setup(shouldShowNavBar: Bool) {
    store.action.accept(.setupShouldShowNavBar(shouldShowNavBar))
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
    
    input.didDisappear
      .asObservable()
      .map { PinCodeAction.clearCode }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    setupBindings()
  }
  
  private func clearCode() {
    store.action.accept(.clearCode)
    didTypeWrongPinCode.accept(())
  }
  
  private func setupBindings() {
    state
      .filter { $0.code.count == PinCodeDotsView.numberOfDots }
      .asObservable()
      .flatMap { [unowned self] state -> Driver<PinCodeState> in
        switch state.stage {
        case .confirmation:
          return self.track(self.confirmPin(for: state)).map { state }
        case .verification:
          return self.track(self.verifyPin(for: state)).map { state }
        default:
          return .just(state)
        }
    }
      
    .subscribe(onNext: { [delegate] in delegate?.didFinishPinCode(for: $0.stage, with: $0.code) })
    .disposed(by: disposeBag)
  }
  
  private func confirmPin(for state: PinCodeState) -> Completable {
    guard state.code == state.correctCode else {
      clearCode()
      return .error(PinCodeError.notMatch)
    }
    
    return usecase.save(pinCode: state.code)
  }
  
  private func verifyPin(for state: PinCodeState) -> Completable {
    return usecase.verify(pinCode: state.code).andThen(usecase.refresh())
      .do(onError: { [unowned self] _ in self.clearCode() })
  }
  
}

import Foundation
import RxSwift
import RxCocoa
import LocalAuthentication

class PinCodePresenter: ModulePresenter, PinCodeModule {
  
  typealias Store = ViewStore<PinCodeAction, PinCodeState>
  
  struct Input {
    var addDigit: Driver<String>
    var removeDigit: Driver<Void>
    var didDisappear: Driver<Void>
    var laAuthDriver: Driver<Void>
  }
  
  private let usecase: PinCodeUsecase
  private let store: Store
  private let balanceService: BalanceService
  private let laContext = LAContext()
    
  let didTypeWrongPinCode = PublishRelay<Void>()
  
  var state: Driver<PinCodeState> {
    return store.state
  }
  
  weak var delegate: PinCodeModuleDelegate?
  
  init(usecase: PinCodeUsecase,
       balanceService: BalanceService,
       store: Store = PinCodeStore()) {
    self.usecase = usecase
    self.store = store
    self.balanceService = balanceService
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
    
    input.laAuthDriver
        .asObservable()
        .subscribe { [weak self] (_) in
            self?.enrollLocalAuth()
        }
        .disposed(by: disposeBag)
    
    setupBindings()
  }
  
  func startLocalAuth() {
     enrollLocalAuth()
  }
    
  private func clearCode() {
    store.action.accept(.clearCode)
    didTypeWrongPinCode.accept(())
  }


    private func enrollLocalAuth() {
       
        guard UserDefaultsHelper.isLocalAuthEnabled else { return }
        
        laContext.enroll { [unowned self] in
            self.usecase
                .refresh()
                .do(onError: { [unowned self] _ in self.clearCode() }, onCompleted: { [unowned self] in
                self.balanceService.start()
                }).subscribe({ [unowned self]  _ in self.delegate?.didFinishPinCode(for: .verification, with: "") })
                .disposed(by: disposeBag)
        } failure: {
            
        }
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
    self.balanceService.start()
    return usecase.save(pinCode: state.code)
  }
  
  private func verifyPin(for state: PinCodeState) -> Completable {
    return usecase.verify(pinCode: state.code).andThen(usecase.refresh())
      .do(onError: { [unowned self] _ in self.clearCode() }, onCompleted: { [unowned self] in
        self.balanceService.start()
      })
  }
  
}

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
    private let balanceService: MainSocketService
    private var laContext = LAContext()
    private var shouldUseLocalAuthOnStart = false
    
    let didTypeWrongPinCode = PublishRelay<Void>()
    
    var state: Driver<PinCodeState> {
        return store.state
    }
    
    weak var delegate: PinCodeModuleDelegate?
    
    init(usecase: PinCodeUsecase,
         balanceService: MainSocketService,
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
    
    func setup(shouldUseLocalAuthOnStart isEnabled: Bool = false) {
        store.action.accept(.localAuthOnStartEnabled(isEnabled))
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
            .doOnNext({ [weak self] in
                self?.refreshContext()
                self?.enrollLocalAuth()
            })
            .subscribe()
            .disposed(by: disposeBag)
        
        setupBindings()
    }
    
    func startLocalAuth() {
        guard UserDefaultsHelper.pinCodeWasEntered,
              store.currentState.isEnabledLocalAuthOnStart else { return }
        enrollLocalAuth()
    }
    
    func refreshContext() {
        laContext = LAContext();
    }
    
    private func clearCode() {
        store.action.accept(.clearCode)
        didTypeWrongPinCode.accept(())
    }
    
    
    private func enrollLocalAuth() {
        
        guard UserDefaultsHelper.isLocalAuthEnabled,
              UserDefaultsHelper.pinCodeWasEntered else { return }
        
        laContext.enroll { [weak self] in
            self?.usecase
                .refresh()
                .do(onError: { [weak self] _ in self?.clearCode() }, onCompleted: { [weak self] in
                    self?.balanceService.start()
                    self?.usecase.startTrades()
                    self?.usecase.startOrdersUpdates()
                }).subscribe({ [weak self]  _ in self?.delegate?.didFinishPinCode(for: .verification, with: "") })
                .disposed(by: self?.disposeBag ?? DisposeBag() )
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
        UserDefaultsHelper.pinCodeWasEntered = true
        self.balanceService.start()
        usecase.startTrades()
        usecase.startOrdersUpdates()
        return usecase.save(pinCode: state.code)
    }
    
    private func verifyPin(for state: PinCodeState) -> Completable {
        return usecase.verify(pinCode: state.code).andThen(usecase.refresh())
            .do(onError: { [unowned self] _ in self.clearCode() }, onCompleted: { [unowned self] in
                self.balanceService.start()
                self.usecase.startTrades()
                self.usecase.startOrdersUpdates()
            })
    }
}

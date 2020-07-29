import Foundation
import RxSwift
import RxCocoa

final class VIPVerificationPresenter: ModulePresenter, VIPVerificationModule {
  
  typealias Store = ViewStore<VIPVerificationAction, VIPVerificationState>
  
  struct Input {
    var back: Driver<Void>
    var select: Driver<Void>
    var remove: Driver<Void>
    var updateSSN: Driver<String?>
    var send: Driver<Void>
  }
  
  private let usecase: SettingsUsecase
  private let store: Store
  
  weak var delegate: VIPVerificationModuleDelegate?
  
  var state: Driver<VIPVerificationState> {
    return store.state
  }
  
  init(usecase: SettingsUsecase,
       store: Store = VIPVerificationStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func didPick(image: UIImage) {
    store.action.accept(.updateSelectedImage(image))
  }
  
  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishVIPVerification(with: nil) })
      .disposed(by: disposeBag)
    
    input.select
      .drive(onNext: { [unowned self] in self.delegate?.showPicker(from: self) })
      .disposed(by: disposeBag)
    
    input.remove
      .drive(onNext: { [store] in store.action.accept(.updateSelectedImage(nil)) })
      .disposed(by: disposeBag)
    
    input.updateSSN
      .drive(onNext:{ [store] in store.action.accept(.updateSSN($0)) })
      .disposed(by: disposeBag)
    
    input.send
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { VIPVerificationUserData(selfieData: $0.selectedImageData!,
                                     ssn: $0.ssn) }
      .flatMap { [unowned self] in self.track(self.sendVIPVerification(userData: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.didFinishVIPVerification(with: $0) })
      .disposed(by: disposeBag)
  }
  
  private func sendVIPVerification(userData: VIPVerificationUserData) -> Single<VerificationInfo> {
    return usecase.sendVIPVerification(userData: userData)
      .andThen(usecase.getVerificationInfo())
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error.message))
        }
        
        throw $0
      }
  }
}

import Foundation
import RxSwift
import RxCocoa

final class VerificationPresenter: ModulePresenter, VerificationModule {
  
  typealias Store = ViewStore<VerificationAction, VerificationState>
  
  struct Input {
    var back: Driver<Void>
    var select: Driver<Void>
    var remove: Driver<Void>
    var updateIDNumber: Driver<String?>
    var updateFirstName: Driver<String?>
    var updateLastName: Driver<String?>
    var updateAddress: Driver<String?>
    var selectCountry: Driver<String>
    var selectProvince: Driver<String>
    var selectCity: Driver<String>
    var updateZipCode: Driver<String?>
    var send: Driver<Void>
  }
  
  private let usecase: SettingsUsecase
  private let store: Store
  
  weak var delegate: VerificationModuleDelegate?
  
  var state: Driver<VerificationState> {
    return store.state
  }
  
  init(usecase: SettingsUsecase,
       store: Store = VerificationStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func didPick(image: UIImage) {
    store.action.accept(.updateSelectedImage(image))
  }
  
  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishVerification(with: nil) })
      .disposed(by: disposeBag)
    
    input.select
      .drive(onNext: { [unowned self] in self.delegate?.showPicker(from: self) })
      .disposed(by: disposeBag)
    
    input.remove
      .drive(onNext: { [store] in store.action.accept(.updateSelectedImage(nil)) })
      .disposed(by: disposeBag)
    
    input.updateIDNumber
      .drive(onNext:{ [store] in store.action.accept(.updateIDNumber($0)) })
      .disposed(by: disposeBag)
    
    input.updateFirstName
      .drive(onNext:{ [store] in store.action.accept(.updateFirstName($0)) })
      .disposed(by: disposeBag)
    
    input.updateLastName
      .drive(onNext:{ [store] in store.action.accept(.updateLastName($0)) })
      .disposed(by: disposeBag)
    
    input.updateAddress
      .drive(onNext:{ [store] in store.action.accept(.updateAddress($0)) })
      .disposed(by: disposeBag)
    
    input.selectCountry
      .drive(onNext: { [store] in store.action.accept(.updateCountry($0)) })
      .disposed(by: disposeBag)
    
    input.selectProvince
      .drive(onNext: { [store] in store.action.accept(.updateProvince($0)) })
      .disposed(by: disposeBag)
    
    input.selectCity
      .drive(onNext: { [store] in store.action.accept(.updateCity($0)) })
      .disposed(by: disposeBag)
    
    input.updateZipCode
      .drive(onNext:{ [store] in store.action.accept(.updateZipCode($0)) })
      .disposed(by: disposeBag)
    
    input.send
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .flatMap { [unowned self] in self.track(self.sendVerification(state: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.didFinishVerification(with: $0) })
      .disposed(by: disposeBag)
  }
  
  private func sendVerification(state: VerificationState) -> Single<VerificationInfo> {
    return Single.just(state)
      .map { VerificationUserData(scanData: $0.selectedImageData!,
                                  idNumber: $0.idNumber,
                                  firstName: $0.firstName,
                                  lastName: $0.lastName,
                                  address: $0.address,
                                  country: $0.country,
                                  province: $0.province,
                                  city: $0.city,
                                  zipCode: $0.zipCode) }
      .flatMapCompletable { [usecase] in usecase.sendVerification(userData: $0) }
      .andThen(usecase.getVerificationInfo())
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error))
        }

        throw $0
      }
  }
}

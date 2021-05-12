import Foundation
import RxSwift
import RxCocoa
import FirebaseStorage

final class VIPVerificationPresenter: ModulePresenter, VIPVerificationModule {
    
    typealias Store = ViewStore<VIPVerificationAction, VIPVerificationState>
    
    struct Input {
        var select: Driver<Void>
        var remove: Driver<Void>
        var updateSSN: Driver<String?>
        var send: Driver<Void>
    }
    let didViewLoad = PublishRelay<Void>()
    
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
        didViewLoad
            .flatMap { [unowned self] _ in
                return self.track(self.usecase.getUserId()
                                    .do(onSuccess: { [store] in store.action.accept(.getUserId($0)) })
                                    .asCompletable())
            }
            .subscribe()
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
        
            .map {
                let selfieFileName = "\($0.userId ?? 0)_ssn_\(String.randomString(length: 10)).jpg"
                return VIPVerificationUserData(userId: $0.userId ?? 0,
                                               selfieData: $0.selectedImageData!,
                                               ssn: $0.ssn,
                                               selfieFileName: selfieFileName)
            }
            .flatMap { [unowned self] in self.track(self.sendVIPVerification(userData: $0)) }
            .subscribe(onNext: { [delegate] _ in delegate?.didFinishVIPVerification() })
            .disposed(by: disposeBag)
    }
    
    private func sendVIPVerification(userData: VIPVerificationUserData) -> Completable {
        setupFirebaseStorage(userId: userData.userId, userData: userData.selfieData, fileName: userData.selfieFileName)
        return usecase.sendVIPVerification(userData: userData)
    }
    
    private func setupFirebaseStorage(userId: Int, userData: Data, fileName: String) {
        let uploadRef = Storage.storage().reference().child("verification").child("\(fileName)")
        let uploadMetadata = StorageMetadata()
        uploadMetadata.contentType = "image/jpeg"
        
        uploadRef.putData(userData, metadata: uploadMetadata) { downloadMetadata, error in
            guard error == nil else {
                print("FUCKING ERROR: \(error!)")
                return
            }
        }
    }
}

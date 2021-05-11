import Foundation
import RxSwift
import RxCocoa

final class KYCPresenter: ModulePresenter, KYCModule {
    struct Input {
        var verify: Driver<Void>
    }
    
    weak var delegate: KYCModuleDelegate?
    
    let kycRelay = PublishRelay<KYC>()
    let didViewLoad = PublishRelay<Void>()
    private let usecase: SettingsUsecase
    
    init(usecase: SettingsUsecase) {
        self.usecase = usecase
    }
    
    func bind(input: Input) {
        didViewLoad
            .flatMap { [unowned self] _ in
                self.track(self.usecase.getKYC()
                            .do(onSuccess: { self.kycRelay.accept($0) })
                            .asCompletable())
            }
            .subscribe()
            .disposed(by: disposeBag)
        
        input.verify
            .asObservable()
            .withLatestFrom(kycRelay)
            .subscribe(onNext: { [unowned self] info in
                if info.status.needVerification {
                    self.delegate?.didSelectVerify(from: self)
                } else if info.status.needVIPVerification {
                    self.delegate?.didSelectVIPVerify(from: self)
                }
            })
            .disposed(by: disposeBag)
    }
}

import Foundation
import RxSwift
import RxCocoa

final class KYCPresenter: ModulePresenter, KYCModule {

  struct Input {
    var verify: Driver<Void>
  }

  weak var delegate: KYCModuleDelegate?
  
  let kycRelay = BehaviorRelay(value: KYC.empty)
  
  func setup(with kyc: KYC) {
    kycRelay.accept(kyc)
  }

  func bind(input: Input) {
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

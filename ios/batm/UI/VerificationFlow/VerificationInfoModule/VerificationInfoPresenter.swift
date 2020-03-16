import Foundation
import RxSwift
import RxCocoa

final class VerificationInfoPresenter: ModulePresenter, VerificationInfoModule {

  struct Input {
    var back: Driver<Void>
    var verify: Driver<Void>
  }

  weak var delegate: VerificationInfoModuleDelegate?
  
  let infoRelay = BehaviorRelay(value: VerificationInfo.empty)
  
  func setup(with info: VerificationInfo) {
    infoRelay.accept(info)
  }

  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishVerificationInfo() })
      .disposed(by: disposeBag)
    
    input.verify
      .asObservable()
      .withLatestFrom(infoRelay)
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

import Foundation
import RxSwift
import RxCocoa

class WelcomePresenter: ModulePresenter, WelcomeModule {
  struct Input {
    var create: Driver<Void>
    var recover: Driver<Void>
    var contactSupport: Driver<Void>
  }
  
  weak var delegate: WelcomeModuleDelegate?
  
  func bind(input: Input) {
    input.create
      .drive(onNext: { [delegate] in delegate?.showCreateWalletScreen() })
      .disposed(by: disposeBag)
    
    input.recover
      .drive(onNext: { [delegate] in delegate?.showRecoverScreen() })
      .disposed(by: disposeBag)
    
    input.contactSupport
      .drive(onNext: { [delegate] in delegate?.showContactSupportAlert() })
      .disposed(by: disposeBag)
  }
}

import Foundation
import RxSwift
import RxCocoa

class WelcomePresenter: ModulePresenter, WelcomeModule {
  struct Input {
    var openTermsAndConditions: Driver<Void>
    var create: Driver<Void>
    var recover: Driver<Void>
    var copy: Driver<String?>
  }
  
  weak var delegate: WelcomeModuleDelegate?
  
  func bind(input: Input) {
    input.openTermsAndConditions
      .drive(onNext: { UIApplication.shared.open(URL.privacyPolicy) })
      .disposed(by: disposeBag)
    
    input.create
      .drive(onNext: { [delegate] in delegate?.showCreateWalletScreen() })
      .disposed(by: disposeBag)
    
    input.recover
      .drive(onNext: { [delegate] in delegate?.showRecoverScreen() })
      .disposed(by: disposeBag)
    
    input.copy
      .drive(onNext: { UIPasteboard.general.string = $0 })
      .disposed(by: disposeBag)
  }
}

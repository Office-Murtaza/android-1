import Foundation
import RxSwift
import RxCocoa

class WelcomePresenter: ModulePresenter, WelcomeModule {
  struct Input {
    var openTermsAndConditions: Driver<Void>
    var create: Driver<Void>
  }
  
  weak var delegate: WelcomeModuleDelegate?
  
  func bind(input: Input) {
    input.openTermsAndConditions
      .drive(onNext: { UIApplication.shared.open(API.privacyPolicyURL) })
      .disposed(by: disposeBag)
    
    input.create
      .drive(onNext: { [delegate] in delegate?.showCreateWalletScreen() })
      .disposed(by: disposeBag)
  }
}

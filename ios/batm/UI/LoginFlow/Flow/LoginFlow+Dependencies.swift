import Foundation
import Swinject

extension LoginFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(LoginFlowController.self) { ioc in
          let flowController = LoginFlowController()
          flowController.delegate = ioc.resolve(LoginFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(WelcomeModuleDelegate.self,
                    CreateWalletModuleDelegate.self,
                    SeedPhraseModuleDelegate.self,
                    RecoverModuleDelegate.self,
                    RecoverSeedPhraseModuleDelegate.self,
                    PinCodeModuleDelegate.self)
    }
  }
}

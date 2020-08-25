import Foundation
import Swinject

extension SettingsFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(SettingsFlowController.self) { ioc in
          let flowController = SettingsFlowController()
          flowController.delegate = ioc.resolve(SettingsFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(SettingsModuleDelegate.self,
                    SecurityModuleDelegate.self,
                    AboutModuleDelegate.self,
                    UpdatePhoneFlowControllerDelegate.self,
                    UpdatePasswordFlowControllerDelegate.self,
                    UpdatePinFlowControllerDelegate.self,
                    KYCFlowControllerDelegate.self,
                    SeedPhraseFlowControllerDelegate.self,
                    UnlinkFlowControllerDelegate.self)
    }
  }
}

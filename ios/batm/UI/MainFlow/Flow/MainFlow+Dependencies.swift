import Foundation
import Swinject

extension MainFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(MainFlowController.self) { ioc in
          let flowController = MainFlowController()
          flowController.delegate = ioc.resolve(MainFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(WalletFlowControllerDelegate.self,
                    ATMFlowControllerDelegate.self,
                    SettingsFlowControllerDelegate.self,
                    DealsFlowControllerDelegate.self)
    }
  }
}

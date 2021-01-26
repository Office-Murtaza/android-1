import Foundation
import Swinject

extension WalletFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(WalletFlowController.self) { ioc in
          let flowController = WalletFlowController()
          flowController.delegate = ioc.resolve(WalletFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(WalletModuleDelegate.self,
                    CoinDetailsFlowControllerDelegate.self)
    }
  }
}

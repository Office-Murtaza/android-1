import Foundation
import Swinject

extension CoinsBalanceFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(CoinsBalanceFlowController.self) { ioc in
          let flowController = CoinsBalanceFlowController()
          flowController.delegate = ioc.resolve(CoinsBalanceFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(CoinsBalanceModuleDelegate.self,
                    FilterCoinsModuleDelegate.self,
                    CoinDetailsFlowControllerDelegate.self)
    }
  }
}

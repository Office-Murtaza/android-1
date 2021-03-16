import Foundation
import Swinject

extension DealsFlow {
    class Dependencies: Assembly {
        func assemble(container: Container) {
            container
                .register(DealsFlowController.self) { ioc in
                    let flowController = DealsFlowController()
                    flowController.delegate = ioc.resolve(DealsFlowControllerDelegate.self)
                    return flowController
                }
                .inObjectScope(.container)
                .implements(DealsModuleDelegate.self,
                            CoinExchangeModuleDelegate.self,
                            CoinStakingModuleDelegate.self)
        }
  }
}

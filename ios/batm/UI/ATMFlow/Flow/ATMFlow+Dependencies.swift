import Foundation
import Swinject

extension ATMFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(ATMFlowController.self) { ioc in
          let flowController = ATMFlowController()
          flowController.delegate = ioc.resolve(ATMFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(ATMModuleDelegate.self)
    }
  }
}

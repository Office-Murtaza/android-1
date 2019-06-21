import Foundation
import Swinject

extension RootFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container.register(RootFlowController.self) { ioc in
        return RootFlowController()
        }.inObjectScope(.container)
        .implements(LoginFlowDelegate.self)
    }
  }
}

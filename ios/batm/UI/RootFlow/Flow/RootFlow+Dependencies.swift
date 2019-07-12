import Foundation
import Swinject

extension RootFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container.register(RootFlowController.self) { ioc in
        let loginUsecase = ioc.resolve(LoginUsecase.self)!
        return RootFlowController(loginUsecase: loginUsecase)
        }.inObjectScope(.container)
        .implements(LoginFlowControllerDelegate.self,
                    MainFlowControllerDelegate.self,
                    PinCodeModuleDelegate.self)
    }
  }
}

import Swinject

extension UpdatePhoneFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(UpdatePhoneFlowController.self) { ioc in
          let flowController = UpdatePhoneFlowController()
          flowController.delegate = ioc.resolve(UpdatePhoneFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(ShowPhoneModuleDelegate.self,
                    EnterPasswordModuleDelegate.self,
                    UpdatePhoneModuleDelegate.self)
    }
  }
}

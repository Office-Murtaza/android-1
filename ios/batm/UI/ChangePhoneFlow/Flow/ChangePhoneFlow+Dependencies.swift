import Swinject

extension ChangePhoneFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(ChangePhoneFlowController.self) { ioc in
          let flowController = ChangePhoneFlowController()
          flowController.delegate = ioc.resolve(ChangePhoneFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(ShowPhoneModuleDelegate.self,
                    EnterPasswordModuleDelegate.self,
                    ChangePhoneModuleDelegate.self)
    }
  }
}

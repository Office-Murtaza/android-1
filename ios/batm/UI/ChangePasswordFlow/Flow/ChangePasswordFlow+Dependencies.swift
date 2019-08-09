import Swinject

extension ChangePasswordFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(ChangePasswordFlowController.self) { ioc in
          let flowController = ChangePasswordFlowController()
          flowController.delegate = ioc.resolve(ChangePasswordFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(ChangePasswordModuleDelegate.self)
    }
  }
}

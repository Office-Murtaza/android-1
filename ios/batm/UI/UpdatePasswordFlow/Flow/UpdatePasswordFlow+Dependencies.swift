import Swinject

extension UpdatePasswordFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(UpdatePasswordFlowController.self) { ioc in
          let flowController = UpdatePasswordFlowController()
          flowController.delegate = ioc.resolve(UpdatePasswordFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(UpdatePasswordModuleDelegate.self)
    }
  }
}

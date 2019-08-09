import Swinject

extension ChangePinFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(ChangePinFlowController.self) { ioc in
          let flowController = ChangePinFlowController()
          flowController.delegate = ioc.resolve(ChangePinFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(ChangePinModuleDelegate.self)
    }
  }
}

import Swinject

extension UpdatePinFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(UpdatePinFlowController.self) { ioc in
          let flowController = UpdatePinFlowController()
          flowController.delegate = ioc.resolve(UpdatePinFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(PinCodeModuleDelegate.self)
    }
  }
}

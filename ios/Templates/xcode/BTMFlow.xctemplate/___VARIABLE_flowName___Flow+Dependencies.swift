import Swinject

extension ___VARIABLE_flowName___Flow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(___VARIABLE_flowName___FlowController.self) { ioc in
          let flowController = ___VARIABLE_flowName___FlowController()
          flowController.delegate = ioc.resolve(___VARIABLE_flowName___FlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
    }
  }
}

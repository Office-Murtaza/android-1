import Swinject

extension TradesFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(TradesFlowController.self) { ioc in
          let flowController = TradesFlowController()
          flowController.delegate = ioc.resolve(TradesFlowControllerDelegate.self)
          return flowController
        }
        .implements(TradesModuleDelegate.self)
        .inObjectScope(.container)
    }
  }
}

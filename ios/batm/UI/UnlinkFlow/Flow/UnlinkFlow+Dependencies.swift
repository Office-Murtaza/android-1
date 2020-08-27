import Swinject

extension UnlinkFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(UnlinkFlowController.self) { ioc in
          let flowController = UnlinkFlowController()
          flowController.delegate = ioc.resolve(UnlinkFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(UnlinkModuleDelegate.self)
    }
  }
}

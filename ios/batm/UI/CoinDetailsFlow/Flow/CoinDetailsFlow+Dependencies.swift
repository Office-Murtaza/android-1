import Swinject

extension CoinDetailsFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(CoinDetailsFlowController.self) { ioc in
          let flowController = CoinDetailsFlowController()
          flowController.delegate = ioc.resolve(CoinDetailsFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(CoinDetailsModuleDelegate.self)
    }
  }
}
import Swinject

extension SeedPhraseFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(SeedPhraseFlowController.self) { ioc in
          let flowController = SeedPhraseFlowController()
          flowController.delegate = ioc.resolve(SeedPhraseFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(EnterPasswordModuleDelegate.self,
                    SeedPhraseModuleDelegate.self)
    }
  }
}

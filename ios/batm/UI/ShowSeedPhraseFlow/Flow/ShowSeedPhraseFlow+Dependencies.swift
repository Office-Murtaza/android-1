import Swinject

extension ShowSeedPhraseFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(ShowSeedPhraseFlowController.self) { ioc in
          let flowController = ShowSeedPhraseFlowController()
          flowController.delegate = ioc.resolve(ShowSeedPhraseFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(EnterPasswordModuleDelegate.self,
                    ShowSeedPhraseModuleDelegate.self)
    }
  }
}

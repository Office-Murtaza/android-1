import RxFlow

class ShowSeedPhraseFlow: BaseFlow<BTMNavigationController, ShowSeedPhraseFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      EnterPasswordAssembly(),
      ShowSeedPhraseAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case enterPassword
    case showSeedPhrase
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .enterPassword:
      let module = resolver.resolve(Module<EnterPasswordModule>.self)!
      return push(module.controller)
    case .showSeedPhrase:
      let module = resolver.resolve(Module<ShowSeedPhraseModule>.self)!
      return push(module.controller)
    }
  }
}


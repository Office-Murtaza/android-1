import RxFlow

class SeedPhraseFlow: BaseFlow<BTMNavigationController, SeedPhraseFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      EnterPasswordAssembly(),
      SeedPhraseAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case enterPassword
    case seedPhrase
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
      module.controller.title = localize(L.SeedPhrase.title)
      return push(module.controller)
    case .seedPhrase:
      let module = resolver.resolve(Module<SeedPhraseModule>.self)!
      module.input.setup(for: .showing)
      return push(module.controller)
    }
  }
}


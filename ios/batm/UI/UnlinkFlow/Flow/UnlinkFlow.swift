import RxFlow

class UnlinkFlow: BaseFlow<BTMNavigationController, UnlinkFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      UnlinkAssembly(),
      EnterPasswordAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case unlink
    case enterPassword
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .unlink:
      let module = resolver.resolve(Module<UnlinkModule>.self)!
      return push(module.controller)
    case .enterPassword:
      let module = resolver.resolve(Module<EnterPasswordModule>.self)!
      return push(module.controller)
    }
  }
}


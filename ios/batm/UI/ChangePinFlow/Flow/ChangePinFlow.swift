import RxFlow

class ChangePinFlow: BaseFlow<BTMNavigationController, ChangePinFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      ChangePinAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case changePin
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .changePin:
      let module = resolver.resolve(Module<ChangePinModule>.self)!
      return push(module.controller)
    }
  }
}


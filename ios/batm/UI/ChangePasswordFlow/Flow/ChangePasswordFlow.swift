import RxFlow

class ChangePasswordFlow: BaseFlow<BTMNavigationController, ChangePasswordFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      ChangePasswordAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case changePassword
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .changePassword:
      let module = resolver.resolve(Module<ChangePasswordModule>.self)!
      return push(module.controller)
    }
  }
}


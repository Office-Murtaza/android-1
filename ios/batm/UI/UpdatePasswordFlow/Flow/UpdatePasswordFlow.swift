import RxFlow

class UpdatePasswordFlow: BaseFlow<BTMNavigationController, UpdatePasswordFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      UpdatePasswordAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case updatePassword
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .updatePassword:
      let module = resolver.resolve(Module<UpdatePasswordModule>.self)!
      return push(module.controller)
    }
  }
}


import RxFlow

class ChangePhoneFlow: BaseFlow<BTMNavigationController, ChangePhoneFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      ShowPhoneAssembly(),
      EnterPasswordAssembly(),
      ChangePhoneAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case showPhone(PhoneNumber)
    case enterPassword
    case changePhone
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case let .showPhone(phoneNumber):
      let module = resolver.resolve(Module<ShowPhoneModule>.self)!
      module.input.setup(with: phoneNumber)
      return push(module.controller)
    case .enterPassword:
      let module = resolver.resolve(Module<EnterPasswordModule>.self)!
      return push(module.controller)
    case .changePhone:
      let module = resolver.resolve(Module<ChangePhoneModule>.self)!
      return push(module.controller)
    }
  }
}


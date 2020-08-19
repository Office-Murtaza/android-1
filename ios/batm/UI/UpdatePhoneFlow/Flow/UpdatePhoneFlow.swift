import RxFlow

class UpdatePhoneFlow: BaseFlow<BTMNavigationController, UpdatePhoneFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      ShowPhoneAssembly(),
      EnterPasswordAssembly(),
      UpdatePhoneAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case showPhone(PhoneNumber)
    case enterPassword
    case updatePhone
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
      module.controller.title = localize(L.ShowPhone.title)
      return push(module.controller)
    case .updatePhone:
      let module = resolver.resolve(Module<UpdatePhoneModule>.self)!
      return replaceLast(module.controller)
    }
  }
}


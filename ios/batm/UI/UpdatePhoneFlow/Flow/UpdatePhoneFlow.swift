import RxFlow

class UpdatePhoneFlow: BaseFlow<BTMNavigationController, UpdatePhoneFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      ShowPhoneAssembly(),
      EnterPasswordAssembly(),
      UpdatePhoneAssembly(),
      PhoneVerificationAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case showPhone(PhoneNumber)
    case enterPassword
    case updatePhone(String)
    case verifyPhone(String)
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
    case let .updatePhone(oldPhoneNumber):
      let module = resolver.resolve(Module<UpdatePhoneModule>.self)!
      module.input.setup(oldPhoneNumber: oldPhoneNumber)
      return replaceLast(module.controller)
    case let .verifyPhone(phoneNumber):
      let module = resolver.resolve(Module<PhoneVerificationModule>.self)!
      module.input.setup(phoneNumber: phoneNumber, for: .updating)
      return replaceLast(module.controller)
    }
  }
}


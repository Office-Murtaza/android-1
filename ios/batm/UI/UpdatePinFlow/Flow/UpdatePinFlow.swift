import RxFlow

class UpdatePinFlow: BaseFlow<BTMNavigationController, UpdatePinFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      PinCodeAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case oldPin(String)
    case newPin
    case confirmNewPin(String)
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }

  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case let .oldPin(pinCode):
      let module = resolver.resolve(Module<PinCodeModule>.self)!
      module.input.setup(for: .setup)
      module.input.setup(for: .new)
      module.input.setup(with: pinCode)
      module.input.setup(shouldShowNavBar: true)
      return push(module.controller)
    case .newPin:
      let module = resolver.resolve(Module<PinCodeModule>.self)!
      module.input.setup(for: .setup)
      module.input.setup(for: .new)
      module.input.setup(shouldShowNavBar: true)
      return push(module.controller)
    case let .confirmNewPin(pinCode):
      let module = resolver.resolve(Module<PinCodeModule>.self)!
      module.input.setup(for: .confirmation)
      module.input.setup(for: .new)
      module.input.setup(with: pinCode)
      module.input.setup(shouldShowNavBar: true)
      return push(module.controller)
    }
  }
}


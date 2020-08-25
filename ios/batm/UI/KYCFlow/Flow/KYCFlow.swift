import RxFlow

class KYCFlow: BaseFlow<BTMNavigationController, KYCFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      KYCAssembly(),
      VerificationAssembly(),
      VIPVerificationAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case kyc(KYC)
    case verification
    case vipVerification
    case showPicker
    case pop
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case let .kyc(kyc):
      let module = resolver.resolve(Module<KYCModule>.self)!
      module.input.setup(with: kyc)
      return push(module.controller)
    case .verification:
      let module = resolver.resolve(Module<VerificationModule>.self)!
      return push(module.controller)
    case .vipVerification:
      let module = resolver.resolve(Module<VIPVerificationModule>.self)!
      return push(module.controller)
    case .showPicker:
      let config = PickerConfig(title: localize(L.Verification.Picker.title),
                                cancelTitle: localize(L.Shared.cancel),
                                items: [camera(), library()])
      let flow = PickerFlow(view: view, parent: self)
      let step = PickerFlow.Steps.start(config)
      return next(flow: flow, step: step)
    case .pop:
      return pop()
    }
  }
  
  private func camera() -> PickerConfig.PickerItem {
    return PickerConfig.PickerItem(itemTitle: localize(L.Verification.Picker.CameraOption.title),
                                   sourceType: .camera,
                                   editable: false)
  }
  
  private func library() -> PickerConfig.PickerItem {
    return PickerConfig.PickerItem(itemTitle: localize(L.Verification.Picker.LibraryOption.title),
                                   sourceType: .library,
                                   editable: false)
  }
}


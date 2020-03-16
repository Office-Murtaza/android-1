import RxFlow
import RxSwift

class SettingsFlow: BaseFlow<BTMNavigationController, SettingsFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      SettingsAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case settings
    case phone(PhoneNumber)
    case changePassword
    case changePin
    case verification(VerificationInfo)
    case showSeedPhrase
    case unlink
    case popToRoot
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .settings:
      let module = resolver.resolve(Module<SettingsModule>.self)!
      module.controller.title = localize(L.Settings.title)
      module.controller.tabBarItem.image = UIImage(named: "tab_bar_settings")
      module.controller.tabBarItem.selectedImage = UIImage(named: "tab_bar_active_settings")
      return push(module.controller, animated: false)
    case let .phone(phoneNumber):
      let flow = ChangePhoneFlow(view: view, parent: self)
      let step = ChangePhoneFlow.Steps.showPhone(phoneNumber)
      return next(flow: flow, step: step)
    case .changePassword:
      let flow = ChangePasswordFlow(view: view, parent: self)
      let step = ChangePasswordFlow.Steps.changePassword
      return next(flow: flow, step: step)
    case .changePin:
      let flow = ChangePinFlow(view: view, parent: self)
      let step = ChangePinFlow.Steps.changePin
      return next(flow: flow, step: step)
    case let .verification(info):
      let flow = VerificationFlow(view: view, parent: self)
      let step = VerificationFlow.Steps.info(info)
      return next(flow: flow, step: step)
    case .showSeedPhrase:
      let flow = ShowSeedPhraseFlow(view: view, parent: self)
      let step = ShowSeedPhraseFlow.Steps.enterPassword
      return next(flow: flow, step: step)
    case .unlink:
      let flow = UnlinkFlow(view: view, parent: self)
      let step = UnlinkFlow.Steps.unlink
      return next(flow: flow, step: step)
    case .popToRoot:
      view.popToRootViewController(animated: true)
      return .none
    }
  }
}


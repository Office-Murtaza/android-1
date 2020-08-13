import RxFlow
import RxSwift

class SettingsFlow: BaseFlow<BTMNavigationController, SettingsFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      SettingsAssembly(),
      SecurityAssembly(),
      AboutAssembly(),
    ]
  }
  
  enum Steps: Step, Equatable {
    case settings
    case security
    case kyc(VerificationInfo)
    case about
    case updatePhone(PhoneNumber)
    case updatePassword
    case updatePIN
    case seedPhrase
    case unlinkWallet
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
    case .security:
      let module = resolver.resolve(Module<SecurityModule>.self)!
      return push(module.controller)
    case .about:
      let module = resolver.resolve(Module<AboutModule>.self)!
      return push(module.controller)
    case let .updatePhone(phoneNumber):
      let flow = ChangePhoneFlow(view: view, parent: self)
      let step = ChangePhoneFlow.Steps.showPhone(phoneNumber)
      return next(flow: flow, step: step)
    case .updatePassword:
      let flow = ChangePasswordFlow(view: view, parent: self)
      let step = ChangePasswordFlow.Steps.changePassword
      return next(flow: flow, step: step)
    case .updatePIN:
      let flow = ChangePinFlow(view: view, parent: self)
      let step = ChangePinFlow.Steps.changePin
      return next(flow: flow, step: step)
    case let .kyc(info):
      let flow = VerificationFlow(view: view, parent: self)
      let step = VerificationFlow.Steps.info(info)
      return next(flow: flow, step: step)
    case .seedPhrase:
      let flow = ShowSeedPhraseFlow(view: view, parent: self)
      let step = ShowSeedPhraseFlow.Steps.enterPassword
      return next(flow: flow, step: step)
    case .unlinkWallet:
      let flow = UnlinkFlow(view: view, parent: self)
      let step = UnlinkFlow.Steps.unlink
      return next(flow: flow, step: step)
    case .popToRoot:
      view.popToRootViewController(animated: true)
      return .none
    }
  }
}


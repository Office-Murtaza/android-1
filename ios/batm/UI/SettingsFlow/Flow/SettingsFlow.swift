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
    case kyc(KYC)
    case about
    case updatePhone(PhoneNumber)
    case updatePassword
    case updatePIN(String)
    case seedPhrase
    case unlinkWallet
    case popToRoot(String?)
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
      let flow = UpdatePhoneFlow(view: view, parent: self)
      let step = UpdatePhoneFlow.Steps.showPhone(phoneNumber)
      return next(flow: flow, step: step)
    case .updatePassword:
      let flow = UpdatePasswordFlow(view: view, parent: self)
      let step = UpdatePasswordFlow.Steps.updatePassword
      return next(flow: flow, step: step)
    case let .updatePIN(pinCode):
      let flow = UpdatePinFlow(view: view, parent: self)
      let step = UpdatePinFlow.Steps.oldPin(pinCode)
      return next(flow: flow, step: step)
    case .seedPhrase:
      let flow = SeedPhraseFlow(view: view, parent: self)
      let step = SeedPhraseFlow.Steps.enterPassword
      return next(flow: flow, step: step)
    case .unlinkWallet:
      let flow = UnlinkFlow(view: view, parent: self)
      let step = UnlinkFlow.Steps.unlink
      return next(flow: flow, step: step)
    case let .kyc(kyc):
      let flow = KYCFlow(view: view, parent: self)
      let step = KYCFlow.Steps.kyc(kyc)
      return next(flow: flow, step: step)
    case let .popToRoot(toastMessage):
      view.popToRootViewController(animated: true)
      toastMessage.flatMap { message in
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
          self.view.topViewController?.view.makeToast(message)
        }
      }
      return .none
    }
  }
}


import RxFlow
import RxSwift

class MainFlow: BaseFlow<BTMTabBarController, MainFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies()
    ]
  }
  
  enum Steps: Step, Equatable {
    case main
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .main:
      let wallet = WalletFlow(view: BTMNavigationController(), parent: self)
      let atm = ATMFlow(view: BTMNavigationController(), parent: self)
      let settings = SettingsFlow(view: BTMNavigationController(), parent: self)
      
      return setTabs(with: [wallet, atm, settings],
                     steppers: [wallet.stepper, atm.stepper, settings.stepper])
    }
  }
}

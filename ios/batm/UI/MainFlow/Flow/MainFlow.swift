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
      let coinsBalance = CoinsBalanceFlow(view: BTMNavigationController(), parent: self)
      let atm = ATMFlow(view: BTMNavigationController(), parent: self)
      let settings = SettingsFlow(view: BTMNavigationController(), parent: self)
      
      return setTabs(with: [coinsBalance, atm, settings],
                     steppers: [coinsBalance.stepper, atm.stepper, settings.stepper])
    }
  }
}

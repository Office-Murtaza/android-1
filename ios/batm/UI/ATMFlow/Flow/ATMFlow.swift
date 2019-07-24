import RxFlow
import RxSwift

class ATMFlow: BaseFlow<BTMNavigationController, ATMFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      ATMAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case atm
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .atm:
      let module = resolver.resolve(Module<ATMModule>.self)!
      module.controller.title = localize(L.Atm.title)
      module.controller.tabBarItem.image = UIImage(named: "tab_bar_atm")
      module.controller.tabBarItem.selectedImage = UIImage(named: "tab_bar_active_atm")
      return push(module.controller, animated: false)
    }
  }
}

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
    }
  }
}


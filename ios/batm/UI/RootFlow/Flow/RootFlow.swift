import RxFlow
import RxSwift

class RootFlow: BaseFlow<UIWindow, RootFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      SplashAssembly(),
      PinCodeAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case splash
    case login
    case logout
    case pinCode(PinCodeStage, String? = nil)
    case main
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .splash:
      let splash = resolver.resolve(Module<SplashModule>.self)!
      return replaceRoot(with: splash)
    case .login:
      let loginFlow = LoginFlow(view: BTMNavigationController(), parent: self)
      return replaceRoot(with: loginFlow)
    case .logout:
      let loginFlow = LoginFlow(view: BTMNavigationController(), parent: self)
      let toastMessage = localize(L.Unlink.unlinked)
      return replaceRoot(with: loginFlow, step: LoginFlow.Steps.welcome(toastMessage))
    case let .pinCode(stage, pinCode):
      let module = resolver.resolve(Module<PinCodeModule>.self)!
      module.input.setup(for: stage)
      
      if stage == .confirmation {
        module.input.setup(shouldShowNavBar: true)
      }
      
      if let pinCode = pinCode {
        module.input.setup(with: pinCode)
      }
      
      return replaceRoot(with: module)
    case .main:
      let tabBarController = BTMTabBarController()
      tabBarController.tabBar.isTranslucent = false
      tabBarController.tabBar.barTintColor = .white
      let mainFlow = MainFlow(view: tabBarController, parent: self)
      return replaceRoot(with: mainFlow)
    }
  }
}

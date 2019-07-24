import RxFlow
import RxSwift

class RootFlow: BaseFlow<UIWindow, RootFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      PinCodeAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case login
    case verifyPinCode
    case main
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .login:
      let loginFlow = LoginFlow(view: BTMNavigationController(), parent: self)
      return replaceRoot(with: loginFlow)
    case .verifyPinCode:
      let module = resolver.resolve(Module<PinCodeModule>.self)!
      module.input.setup(for: .verification)
      return replaceRoot(with: module)
    case .main:
      let mainFlow = MainFlow(view: BTMTabBarController(), parent: self)
      return replaceRoot(with: mainFlow)
    }
  }
}

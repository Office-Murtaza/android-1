import RxFlow
import RxSwift

class RootFlow: BaseFlow<UIWindow, RootFlowController> {
  override func assemblies() -> [Assembly] {
    return []
  }
  
  enum Steps: Step, Equatable {
    case login
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
    }
  }
}

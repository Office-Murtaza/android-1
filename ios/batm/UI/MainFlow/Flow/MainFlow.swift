import RxFlow
import RxSwift

class MainFlow: BaseFlow<BTMNavigationController, MainFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      CoinsBalanceAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case coinsBalance
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .coinsBalance:
      let module = resolver.resolve(Module<CoinsBalanceModule>.self)!
      return replaceRoot(module.controller, animated: false)
    }
  }
}

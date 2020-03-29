import RxFlow
import RxSwift

class CoinsBalanceFlow: BaseFlow<BTMNavigationController, CoinsBalanceFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      CoinsBalanceAssembly(),
      FIlterCoinsAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case coinsBalance
    case filterCoins
    case coinDetails(CoinBalance, CoinSettings, PriceChartData)
    case pop
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
      module.controller.title = localize(L.CoinsBalance.title)
      module.controller.tabBarItem.image = UIImage(named: "tab_bar_wallet")
      module.controller.tabBarItem.selectedImage = UIImage(named: "tab_bar_active_wallet")
      return push(module.controller, animated: false)
    case .filterCoins:
      let module = resolver.resolve(Module<FilterCoinsModule>.self)!
      return push(module.controller)
    case let .coinDetails(coinBalance, coinSettings, data):
      let flow = CoinDetailsFlow(view: view, parent: self)
      let step = CoinDetailsFlow.Steps.coinDetails(coinBalance, coinSettings, data)
      return next(flow: flow, step: step)
    case .pop: return pop()
    }
  }
}

import RxFlow
import RxSwift

class WalletFlow: BaseFlow<BTMNavigationController, WalletFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      WalletAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case wallet
    case coinDetails([CoinBalance], CoinDetails, PriceChartDetails)
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .wallet:
      let module = resolver.resolve(Module<WalletModule>.self)!
      module.controller.title = localize(L.Wallet.title)
      module.controller.tabBarItem.image = UIImage(named: "tab_bar_wallet")
      module.controller.tabBarItem.selectedImage = UIImage(named: "tab_bar_active_wallet")
      return push(module.controller, animated: false)
    case let .coinDetails(coinBalances, coinDetails, data):
      let flow = CoinDetailsFlow(view: view, parent: self)
      let step = CoinDetailsFlow.Steps.coinDetails(coinBalances, coinDetails, data)
      return next(flow: flow, step: step)
    }
  }
}

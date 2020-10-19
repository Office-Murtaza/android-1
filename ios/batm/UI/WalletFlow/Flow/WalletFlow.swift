import RxFlow
import RxSwift

class WalletFlow: BaseFlow<BTMNavigationController, WalletFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      WalletAssembly(),
      ManageWalletsAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case wallet
    case manageWallets
    case coinDetails([CoinBalance], CoinDetails, PriceChartData)
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
    case .manageWallets:
      let module = resolver.resolve(Module<ManageWalletsModule>.self)!
      return push(module.controller)
    case let .coinDetails(coinBalances, coinDetails, data):
      let flow = CoinDetailsFlow(view: view, parent: self)
      let step = CoinDetailsFlow.Steps.coinDetails(coinBalances, coinDetails, data)
      return next(flow: flow, step: step)
    }
  }
}

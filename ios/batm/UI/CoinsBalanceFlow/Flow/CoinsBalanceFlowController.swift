import Foundation
import RxSwift
import RxFlow

protocol CoinsBalanceFlowControllerDelegate: class {}

class CoinsBalanceFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = CoinsBalanceFlow.Steps.coinsBalance
  
  weak var delegate: CoinsBalanceFlowControllerDelegate?
  weak var module: CoinsBalanceModule?
  
}

extension CoinsBalanceFlowController: CoinsBalanceModuleDelegate {
  
  func showManageWallets(from module: CoinsBalanceModule) {
    self.module = module
    step.accept(CoinsBalanceFlow.Steps.manageWallets)
  }
  
  func showCoinDetails(coinBalances: [CoinBalance], coinSettings: CoinSettings, data: PriceChartData) {
    step.accept(CoinsBalanceFlow.Steps.coinDetails(coinBalances, coinSettings, data))
  }
  
}

extension CoinsBalanceFlowController: ManageWalletsModuleDelegate {
  func didChangeVisibility() {
    module?.fetchCoinsBalance()
  }
}

extension CoinsBalanceFlowController: CoinDetailsFlowControllerDelegate {
  func didFinishCoinDetailsFlow() {
    step.accept(CoinsBalanceFlow.Steps.pop)
  }
}

import Foundation
import RxSwift
import RxFlow

protocol WalletFlowControllerDelegate: class {}

class WalletFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = WalletFlow.Steps.wallet
  
  weak var delegate: WalletFlowControllerDelegate?
  weak var module: WalletModule?
  
}

extension WalletFlowController: WalletModuleDelegate {
  
  func showManageWallets(from module: WalletModule) {
    self.module = module
    step.accept(WalletFlow.Steps.manageWallets)
  }
  
  func showCoinDetails(coinBalances: [CoinBalance], coinDetails: CoinDetails, data: PriceChartData) {
    step.accept(WalletFlow.Steps.coinDetails(coinBalances, coinDetails, data))
  }
  
}

extension WalletFlowController: ManageWalletsModuleDelegate {
  func didChangeVisibility() {
    module?.fetchCoinsBalance()
  }
}

extension WalletFlowController: CoinDetailsFlowControllerDelegate {}

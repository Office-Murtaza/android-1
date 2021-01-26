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
  func showCoinDetails(coinBalances: [CoinBalance], coinDetails: CoinDetails, data: PriceChartDetails) {
    step.accept(WalletFlow.Steps.coinDetails(coinBalances, coinDetails, data))
  }
  
  func showCoinDetail(predefinedConfig: CoinDetailsPredefinedDataConfig) {
    step.accept(WalletFlow.Steps.coinDetailsPredefinedData(predefinedConfig))
  }
}

extension WalletFlowController: CoinDetailsFlowControllerDelegate {}

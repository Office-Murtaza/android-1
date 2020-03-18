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
  
  func showFilterCoins(from module: CoinsBalanceModule) {
    self.module = module
    step.accept(CoinsBalanceFlow.Steps.filterCoins)
  }
  
  func showCoinDetails(with coinBalance: CoinBalance, and data: PriceChartData) {
    step.accept(CoinsBalanceFlow.Steps.coinDetails(coinBalance, data))
  }
  
}

extension CoinsBalanceFlowController: FilterCoinsModuleDelegate {
  func didFinishFiltering() {
    step.accept(CoinsBalanceFlow.Steps.pop)
  }
  
  func didChangeVisibility() {
    module?.fetchCoinsBalance()
  }
}

extension CoinsBalanceFlowController: CoinDetailsFlowControllerDelegate {
  func didFinishCoinDetailsFlow() {
    step.accept(CoinsBalanceFlow.Steps.pop)
  }
}

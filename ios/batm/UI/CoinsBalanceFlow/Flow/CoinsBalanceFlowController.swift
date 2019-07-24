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
  
}

extension CoinsBalanceFlowController: FilterCoinsModuleDelegate {
  func didFinishFiltering() {
    step.accept(CoinsBalanceFlow.Steps.pop)
  }
  
  func didChangeVisibility() {
    module?.fetchCoinsBalance()
  }
}

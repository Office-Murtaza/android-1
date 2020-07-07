import RxFlow

protocol TradesFlowControllerDelegate: class {
  func didFinishTradesFlow()
}

class TradesFlowController: FlowController {
  
  weak var delegate: TradesFlowControllerDelegate?
  
}

extension TradesFlowController: TradesModuleDelegate {
  
  func didFinishTrades() {
    delegate?.didFinishTradesFlow()
  }
  
  func showBuySellTradeDetails(coinBalance: CoinBalance, trade: BuySellTrade, type: TradeType) {
    step.accept(TradesFlow.Steps.buySellTradeDetails(coinBalance, trade, type))
  }
  
  func showCreateEditTrade(coinBalance: CoinBalance) {
    step.accept(TradesFlow.Steps.createEditTrade(coinBalance))
  }
  
  func showReserve(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings) {
    step.accept(TradesFlow.Steps.reserve(coin, coinBalances, coinSettings))
  }
  
  func showRecall(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings) {
    step.accept(TradesFlow.Steps.recall(coin, coinBalances, coinSettings))
  }
  
}

extension TradesFlowController: BuySellTradeDetailsModuleDelegate {
  
  func didFinishBuySellTradeDetails() {
    step.accept(TradesFlow.Steps.pop)
  }
  
}

extension TradesFlowController: CreateEditTradeModuleDelegate {
  
  func didFinishCreateEditTrade() {
    step.accept(TradesFlow.Steps.pop)
  }
  
}

extension TradesFlowController: ReserveModuleDelegate {
  
  func didFinishReserve() {
    step.accept(TradesFlow.Steps.pop)
  }

}

extension TradesFlowController: RecallModuleDelegate {
  
  func didFinishRecall() {
    step.accept(TradesFlow.Steps.pop)
  }

}

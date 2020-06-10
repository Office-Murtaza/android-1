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
  
}

extension TradesFlowController: BuySellTradeDetailsModuleDelegate {
  
  func didFinishBuySellTradeDetails() {
    step.accept(TradesFlow.Steps.pop)
  }
  
}

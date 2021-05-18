import RxFlow

protocol TradesFlowControllerDelegate: AnyObject {
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
    func didFinishReserve(with transactionResult: String, transactionDetails: TransactionDetails?) {
        step.accept(TradesFlow.Steps.pop)
    }
}

extension TradesFlowController: RecallModuleDelegate {
    func didFinishRecall(with transactionResult: String, transactionDetails: TransactionDetails?) {
        step.accept(TradesFlow.Steps.pop)
    }
}

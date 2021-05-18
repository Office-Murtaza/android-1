import RxFlow
import TrustWalletCore

protocol CoinDetailsFlowControllerDelegate: AnyObject {}

class CoinDetailsFlowController: FlowController {
    weak var delegate: CoinDetailsFlowControllerDelegate?
}

extension CoinDetailsFlowController: CoinDetailsModuleDelegate {
    func showTransactionDetails(with transactionDetails: TransactionDetails, coinType: CustomCoinType) {
        step.accept(CoinDetailsFlow.Steps.transactionDetails(transactionDetails, coinType))
    }
    
    func showDepositScreen(with coinType: CustomCoinType) {
        step.accept(CoinDetailsFlow.Steps.deposit(coinType))
    }
    
    func showWithdrawScreen(with coinType: CustomCoinType) {
        step.accept(CoinDetailsFlow.Steps.withdraw(coinType))
    }
    
    func showReserve(with coinType: CustomCoinType) {
        step.accept(CoinDetailsFlow.Steps.reserve(coinType))
    }
    
    func showRecall(with coinType: CustomCoinType) {
        step.accept(CoinDetailsFlow.Steps.recall(coinType))
    }
}

extension CoinDetailsFlowController: CoinDepositModuleDelegate {}

extension CoinDetailsFlowController: CoinWithdrawModuleDelegate {
    func didFinishCoinWithdraw(with transactionResult: String, transactionDetails: TransactionDetails?) {
        step.accept(CoinDetailsFlow.Steps.updateCoinDetails(localize(transactionResult), transactionDetails))
    }
}

extension CoinDetailsFlowController: CoinSellModuleDelegate {
    func showSellDetailsForAnotherAddress(_ details: SellDetailsForAnotherAddress) {
        step.accept(CoinDetailsFlow.Steps.sellDetailsForAnotherAddress(details))
    }
    
    func showSellDetailsForCurrentAddress(_ details: SellDetailsForCurrentAddress) {
        step.accept(CoinDetailsFlow.Steps.sellDetailsForCurrentAddress(details))
    }
    
    func didFinishCoinSell() {
        step.accept(CoinDetailsFlow.Steps.pop(nil))
    }
}

extension CoinDetailsFlowController: CoinSellDetailsAnotherAddressModuleDelegate {
    func didFinishCoinSellDetailsAnotherAddress() {
        step.accept(CoinDetailsFlow.Steps.pop(nil))
    }
}

extension CoinDetailsFlowController: CoinSellDetailsCurrentAddressModuleDelegate {
    func didFinishCoinSellDetailsCurrentAddress() {
        step.accept(CoinDetailsFlow.Steps.pop(nil))
    }
}

extension CoinDetailsFlowController: CoinExchangeModuleDelegate {
    func didFinishCoinExchange() {
        step.accept(CoinDetailsFlow.Steps.pop(localize(L.CoinDetails.transactionCreated)))
    }
    
    func handleError() {
        step.accept(CoinDetailsFlow.Steps.pop())
    }
}

extension CoinDetailsFlowController: TransactionDetailsModuleDelegate {
    func didFinishTransactionDetails() {
        step.accept(CoinDetailsFlow.Steps.pop(localize(L.CoinDetails.transactionCreated)))
    }
}

extension CoinDetailsFlowController: ReserveModuleDelegate {
    func didFinishReserve(with transactionResult: String, transactionDetails: TransactionDetails?) {
        step.accept(CoinDetailsFlow.Steps.updateCoinDetails(localize(transactionResult), transactionDetails))
    }
}

extension CoinDetailsFlowController: RecallModuleDelegate {
    func didFinishRecall(with transactionResult: String, transactionDetails: TransactionDetails?) {
        step.accept(CoinDetailsFlow.Steps.updateCoinDetails(localize(transactionResult), transactionDetails))
    }
}

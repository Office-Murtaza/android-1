import RxFlow
import TrustWalletCore

protocol CoinDetailsFlowControllerDelegate: class {}

class CoinDetailsFlowController: FlowController {
  
  weak var delegate: CoinDetailsFlowControllerDelegate?
  
}

extension CoinDetailsFlowController: CoinDetailsModuleDelegate {
  
  func showDepositScreen(coin: BTMCoin) {
    step.accept(CoinDetailsFlow.Steps.deposit(coin))
  }
  
  func showWithdrawScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails) {
    step.accept(CoinDetailsFlow.Steps.withdraw(coin, coinBalances, coinDetails))
  }
  
  func showSendGiftScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails) {
    step.accept(CoinDetailsFlow.Steps.sendGift(coin, coinBalances, coinDetails))
  }
  
  func showSellScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails, details: SellDetails) {
    step.accept(CoinDetailsFlow.Steps.sell(coin, coinBalances, coinDetails, details))
  }
  
  func showTransactionDetails(with details: TransactionDetails, coinType: CustomCoinType) {
    step.accept(CoinDetailsFlow.Steps.transactionDetails(details, coinType))
  }
  
  func showExchangeScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails) {
    step.accept(CoinDetailsFlow.Steps.exchange(coin, coinBalances, coinDetails))
  }
  
  func showTradesScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails) {
    step.accept(CoinDetailsFlow.Steps.trades(coin, coinBalances, coinDetails))
  }
  
  func showReserve(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails) {
    step.accept(CoinDetailsFlow.Steps.reserve(coin, coinBalances, coinDetails))
  }
    
  func showRecall(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails) {
    step.accept(CoinDetailsFlow.Steps.recall(coin, coinBalances, coinDetails))
  }
}

extension CoinDetailsFlowController: CoinDepositModuleDelegate {}

extension CoinDetailsFlowController: CoinWithdrawModuleDelegate {
    func didFinishCoinWithdraw(with transactionResult: String) {
        step.accept(CoinDetailsFlow.Steps.pop(localize(transactionResult)))
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
}

extension CoinDetailsFlowController: TransactionDetailsModuleDelegate {
  func didFinishTransactionDetails() {
    step.accept(CoinDetailsFlow.Steps.pop(localize(L.CoinDetails.transactionCreated)))
  }
}

extension CoinDetailsFlowController: TradesFlowControllerDelegate {
  func didFinishTradesFlow() {
    step.accept(CoinDetailsFlow.Steps.pop(localize(L.CoinDetails.transactionCreated)))
  }
}

extension CoinDetailsFlowController: ReserveModuleDelegate {
    func didFinishReserve(with transactionResult: String) {
        step.accept(CoinDetailsFlow.Steps.pop(localize(transactionResult)))
    }
}

extension CoinDetailsFlowController: RecallModuleDelegate {
    func didFinishRecall(with transactionResult: String) {
        step.accept(CoinDetailsFlow.Steps.pop(localize(transactionResult)))
  }
}

import RxFlow
import TrustWalletCore

protocol CoinDetailsFlowControllerDelegate: class {
  func didFinishCoinDetailsFlow()
}

class CoinDetailsFlowController: FlowController {
  
  weak var delegate: CoinDetailsFlowControllerDelegate?
  
}

extension CoinDetailsFlowController: CoinDetailsModuleDelegate {
  
  func didFinishCoinDetails() {
    delegate?.didFinishCoinDetailsFlow()
  }
  
  func showDepositScreen(coin: BTMCoin) {
    step.accept(CoinDetailsFlow.Steps.deposit(coin))
  }
  
  func showWithdrawScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings) {
    step.accept(CoinDetailsFlow.Steps.withdraw(coin, coinBalances, coinSettings))
  }
  
  func showSendGiftScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings) {
    step.accept(CoinDetailsFlow.Steps.sendGift(coin, coinBalances, coinSettings))
  }
  
  func showSellScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings, details: SellDetails) {
    step.accept(CoinDetailsFlow.Steps.sell(coin, coinBalances, coinSettings, details))
  }
  
  func showTransactionDetails(with details: TransactionDetails, for type: CustomCoinType) {
    step.accept(CoinDetailsFlow.Steps.transactionDetails(details, type))
  }
  
  func showExchangeScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings) {
    step.accept(CoinDetailsFlow.Steps.exchange(coin, coinBalances, coinSettings))
  }
  
  func showTradesScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings) {
    step.accept(CoinDetailsFlow.Steps.trades(coin, coinBalances, coinSettings))
  }
  
}

extension CoinDetailsFlowController: CoinDepositModuleDelegate {
  
  func didFinishCoinDeposit() {
    step.accept(CoinDetailsFlow.Steps.pop)
  }
  
}

extension CoinDetailsFlowController: CoinWithdrawModuleDelegate {
  
  func didFinishCoinWithdraw() {
    step.accept(CoinDetailsFlow.Steps.pop)
  }
  
}

extension CoinDetailsFlowController: CoinSendGiftModuleDelegate {
  
  func didFinishCoinSendGift() {
    step.accept(CoinDetailsFlow.Steps.pop)
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
    step.accept(CoinDetailsFlow.Steps.pop)
  }
  
}

extension CoinDetailsFlowController: CoinSellDetailsAnotherAddressModuleDelegate {
  
  func didFinishCoinSellDetailsAnotherAddress() {
    step.accept(CoinDetailsFlow.Steps.pop)
  }
  
}

extension CoinDetailsFlowController: CoinSellDetailsCurrentAddressModuleDelegate {
  
  func didFinishCoinSellDetailsCurrentAddress() {
    step.accept(CoinDetailsFlow.Steps.pop)
  }
  
}

extension CoinDetailsFlowController: CoinExchangeModuleDelegate {
  
  func didFinishCoinExchange() {
    step.accept(CoinDetailsFlow.Steps.pop)
  }
  
}

extension CoinDetailsFlowController: TransactionDetailsModuleDelegate {
  
  func didFinishTransactionDetails() {
    step.accept(CoinDetailsFlow.Steps.pop)
  }
  
}

extension CoinDetailsFlowController: TradesFlowControllerDelegate {
  
  func didFinishTradesFlow() {
    step.accept(CoinDetailsFlow.Steps.pop)
  }
  
}

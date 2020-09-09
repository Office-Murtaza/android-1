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
  
  func showStakingScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings, stakeDetails: StakeDetails) {
    step.accept(CoinDetailsFlow.Steps.staking(coin, coinBalances, coinSettings, stakeDetails))
  }
  
}

extension CoinDetailsFlowController: CoinDepositModuleDelegate {}

extension CoinDetailsFlowController: CoinWithdrawModuleDelegate {
  
  func didFinishCoinWithdraw() {
    step.accept(CoinDetailsFlow.Steps.pop(localize(L.CoinDetails.transactionCreated)))
  }
  
}

extension CoinDetailsFlowController: CoinSendGiftModuleDelegate {
  
  func didFinishCoinSendGift() {
    step.accept(CoinDetailsFlow.Steps.pop(localize(L.CoinDetails.transactionCreated)))
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
    step.accept(CoinDetailsFlow.Steps.pop(nil))
  }
  
}

extension CoinDetailsFlowController: CoinStakingModuleDelegate {
  
  func didFinishCoinStaking() {
    step.accept(CoinDetailsFlow.Steps.pop(nil))
  }
  
}

extension CoinDetailsFlowController: TransactionDetailsModuleDelegate {
  
  func didFinishTransactionDetails() {
    step.accept(CoinDetailsFlow.Steps.pop(nil))
  }
  
}

extension CoinDetailsFlowController: TradesFlowControllerDelegate {
  
  func didFinishTradesFlow() {
    step.accept(CoinDetailsFlow.Steps.pop(nil))
  }
  
}

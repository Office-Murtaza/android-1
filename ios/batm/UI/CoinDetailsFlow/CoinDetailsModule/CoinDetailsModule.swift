import Foundation
import TrustWalletCore

protocol CoinDetailsModule: class {
  func setup(coinBalances: [CoinBalance], coinSettings: CoinSettings, data: PriceChartData)
}
protocol CoinDetailsModuleDelegate: class {
  func showDepositScreen(coin: BTMCoin)
  func showWithdrawScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings)
  func showSendGiftScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings)
  func showSellScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings, details: SellDetails)
  func showTransactionDetails(with details: TransactionDetails, for type: CustomCoinType)
  func showExchangeScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings)
  func showTradesScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings)
  func showStakingScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings, stakeDetails: StakeDetails)
}

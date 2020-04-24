import Foundation
import TrustWalletCore

protocol CoinDetailsModule: class {
  func setup(coinBalances: [CoinBalance], coinSettings: CoinSettings, data: PriceChartData)
}
protocol CoinDetailsModuleDelegate: class {
  func didFinishCoinDetails()
  func showDepositScreen(coin: BTMCoin)
  func showWithdrawScreen(coin: BTMCoin, coinBalance: CoinBalance, coinSettings: CoinSettings)
  func showSendGiftScreen(coin: BTMCoin, coinBalance: CoinBalance, coinSettings: CoinSettings)
  func showSellScreen(coin: BTMCoin, coinBalance: CoinBalance, coinSettings: CoinSettings, details: SellDetails)
  func showTransactionDetails(with details: TransactionDetails, for type: CoinType)
  func showExchangeScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings)
}

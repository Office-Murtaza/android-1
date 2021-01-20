import Foundation
import TrustWalletCore

protocol CoinDetailsModule: class {
  func setup(coinBalances: [CoinBalance], coinDetails: CoinDetails, data: PriceChartDetails)
  func setup(predefinedData: CoinDetailsPredefinedDataConfig)
}
protocol CoinDetailsModuleDelegate: class {
  func showDepositScreen(coin: BTMCoin)
  func showWithdrawScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
  func showSendGiftScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
  func showSellScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails, details: SellDetails)
  func showTransactionDetails(with details: TransactionDetails, for type: CustomCoinType)
  func showExchangeScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
  func showTradesScreen(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
  func showReserve(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
  func showRecall(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
}

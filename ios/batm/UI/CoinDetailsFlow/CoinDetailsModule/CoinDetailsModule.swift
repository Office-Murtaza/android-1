import Foundation
import TrustWalletCore

protocol CoinDetailsModule: class {
  func setup(with coinBalance: CoinBalance)
  func setup(with coinSettings: CoinSettings)
  func setup(with data: PriceChartData)
}
protocol CoinDetailsModuleDelegate: class {
  func didFinishCoinDetails()
  func showWithdrawScreen(coin: BTMCoin, coinBalance: CoinBalance, coinSettings: CoinSettings)
  func showSendGiftScreen(coin: BTMCoin, coinBalance: CoinBalance, coinSettings: CoinSettings)
  func showSellScreen(coin: BTMCoin, coinBalance: CoinBalance, coinSettings: CoinSettings, details: SellDetails)
  func showTransactionDetails(with details: TransactionDetails, for type: CoinType)
}

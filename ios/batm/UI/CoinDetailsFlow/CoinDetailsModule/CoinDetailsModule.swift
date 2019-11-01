import Foundation

protocol CoinDetailsModule: class {
  func setup(with coinBalance: CoinBalance)
}
protocol CoinDetailsModuleDelegate: class {
  func didFinishCoinDetails()
  func showWithdrawScreen(for coin: BTMCoin, and coinBalance: CoinBalance)
  func showSendGiftScreen(for coin: BTMCoin, and coinBalance: CoinBalance)
  func showSellScreen(coin: BTMCoin, coinBalance: CoinBalance, details: SellDetails)
  func showTransactionDetails(for details: TransactionDetails)
}

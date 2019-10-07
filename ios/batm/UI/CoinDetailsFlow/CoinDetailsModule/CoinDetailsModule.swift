import Foundation

protocol CoinDetailsModule: class {
  func setup(with coinBalance: CoinBalance)
}
protocol CoinDetailsModuleDelegate: class {
  func didFinishCoinDetails()
  func showWithdrawScreen(for coin: BTMCoin, and coinBalance: CoinBalance)
  func showSendGiftScreen(for coin: BTMCoin, and coinBalance: CoinBalance)
}

import Foundation

protocol CoinWithdrawModule: class {
  func setup(coin: BTMCoin, coinBalance: CoinBalance, coinSettings: CoinSettings)
}
protocol CoinWithdrawModuleDelegate: class {
  func didFinishCoinWithdraw()
}

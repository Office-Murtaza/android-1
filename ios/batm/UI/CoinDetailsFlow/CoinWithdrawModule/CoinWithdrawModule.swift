import Foundation

protocol CoinWithdrawModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings)
}
protocol CoinWithdrawModuleDelegate: class {
  func didFinishCoinWithdraw()
}

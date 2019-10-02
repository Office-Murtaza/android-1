import Foundation

protocol CoinWithdrawModule: class {
  func setup(with coin: BTMCoin)
  func setup(with coinBalance: CoinBalance)
}
protocol CoinWithdrawModuleDelegate: class {
  func didFinishCoinWithdraw()
}

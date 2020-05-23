import Foundation

protocol TradesModule: class {
  func setup(coinBalance: CoinBalance)
}
protocol TradesModuleDelegate: class {
  func didFinishTrades()
}

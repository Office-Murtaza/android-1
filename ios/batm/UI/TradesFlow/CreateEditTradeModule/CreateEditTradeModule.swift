import Foundation

protocol CreateEditTradeModule: class {
  func setup(coinBalance: CoinBalance)
}
protocol CreateEditTradeModuleDelegate: class {
  func didFinishCreateEditTrade()
}

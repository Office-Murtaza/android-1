import Foundation

protocol CreateEditTradeModule: AnyObject {
  func setup(coinBalance: CoinBalance)
}
protocol CreateEditTradeModuleDelegate: AnyObject {
  func didFinishCreateEditTrade()
}

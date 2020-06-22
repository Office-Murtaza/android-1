import Foundation

protocol BuySellTradeDetailsModule: class {
  func setup(coinBalance: CoinBalance, trade: BuySellTrade, type: TradeType)
}
protocol BuySellTradeDetailsModuleDelegate: class {
  func didFinishBuySellTradeDetails()
}

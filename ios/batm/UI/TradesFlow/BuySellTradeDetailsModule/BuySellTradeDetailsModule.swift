import Foundation

protocol BuySellTradeDetailsModule: AnyObject {
  func setup(coinBalance: CoinBalance, trade: BuySellTrade, type: TradeType)
}
protocol BuySellTradeDetailsModuleDelegate: AnyObject {
  func didFinishBuySellTradeDetails()
}

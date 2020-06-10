import Foundation

protocol TradesModule: class {
  func setup(coinBalance: CoinBalance)
}
protocol TradesModuleDelegate: class {
  func didFinishTrades()
  func showBuySellTradeDetails(coinBalance: CoinBalance, trade: BuySellTrade, type: TradeType)
}

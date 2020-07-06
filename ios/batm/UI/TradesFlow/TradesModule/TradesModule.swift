import Foundation

protocol TradesModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings)
}
protocol TradesModuleDelegate: class {
  func didFinishTrades()
  func showBuySellTradeDetails(coinBalance: CoinBalance, trade: BuySellTrade, type: TradeType)
  func showCreateEditTrade(coinBalance: CoinBalance)
  func showReserve(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings)
  func showRecall(coin: BTMCoin, coinBalances: [CoinBalance])
}

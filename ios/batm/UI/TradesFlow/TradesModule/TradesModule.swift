import Foundation

protocol TradesModule: AnyObject {
    func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
}
protocol TradesModuleDelegate: AnyObject {
    func didFinishTrades()
    func showBuySellTradeDetails(coinBalance: CoinBalance, trade: BuySellTrade, type: TradeType)
    func showCreateEditTrade(coinBalance: CoinBalance)
}

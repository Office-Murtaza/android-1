import Foundation

protocol CoinsBalanceModule: class {
  func fetchCoinsBalance()
}
protocol CoinsBalanceModuleDelegate: class {
  func showManageWallets(from module: CoinsBalanceModule)
  func showCoinDetails(coinBalances: [CoinBalance], coinSettings: CoinSettings, data: PriceChartData)
}

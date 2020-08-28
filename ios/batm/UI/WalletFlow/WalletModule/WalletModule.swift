import Foundation

protocol WalletModule: class {
  func fetchCoinsBalance()
}
protocol WalletModuleDelegate: class {
  func showManageWallets(from module: WalletModule)
  func showCoinDetails(coinBalances: [CoinBalance], coinSettings: CoinSettings, data: PriceChartData)
}

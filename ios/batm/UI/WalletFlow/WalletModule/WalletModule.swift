import Foundation

protocol WalletModule: class {
  func fetchCoinsBalance()
}
protocol WalletModuleDelegate: class {
  func showManageWallets(from module: WalletModule)
  func showCoinDetails(coinBalances: [CoinBalance], coinDetails: CoinDetails, data: PriceChartData)
}

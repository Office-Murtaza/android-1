import Foundation

protocol WalletModule: class {
  func fetchCoinsBalance()
}
protocol WalletModuleDelegate: class {
  func showCoinDetails(coinBalances: [CoinBalance], coinDetails: CoinDetails, data: PriceChartDetails)
  func showCoinDetail(predefinedConfig: CoinDetailsPredefinedDataConfig)
}

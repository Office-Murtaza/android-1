import Foundation

protocol CoinExchangeModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
}
protocol CoinExchangeModuleDelegate: class {
  func didFinishCoinExchange()
}

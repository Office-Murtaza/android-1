import Foundation

protocol CoinExchangeModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings)
}
protocol CoinExchangeModuleDelegate: class {
  func didFinishCoinExchange()
}

import Foundation

protocol CoinExchangeModule: class {
  func setup()
}
protocol CoinExchangeModuleDelegate: class {
  func didFinishCoinExchange()
}

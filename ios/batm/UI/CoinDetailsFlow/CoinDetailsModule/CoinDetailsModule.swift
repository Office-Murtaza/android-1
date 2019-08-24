import Foundation

protocol CoinDetailsModule: class {
  func setup(with coinBalance: CoinBalance)
}
protocol CoinDetailsModuleDelegate: class {
  func didFinishCoinDetails()
}

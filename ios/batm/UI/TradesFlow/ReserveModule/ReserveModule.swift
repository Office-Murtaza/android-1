import Foundation

protocol ReserveModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings)
}
protocol ReserveModuleDelegate: class {
  func didFinishReserve()
}

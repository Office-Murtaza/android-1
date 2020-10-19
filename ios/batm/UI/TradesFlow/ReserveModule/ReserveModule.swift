import Foundation

protocol ReserveModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
}
protocol ReserveModuleDelegate: class {
  func didFinishReserve()
}

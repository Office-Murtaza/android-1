import Foundation

protocol RecallModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings)
}
protocol RecallModuleDelegate: class {
  func didFinishRecall()
}

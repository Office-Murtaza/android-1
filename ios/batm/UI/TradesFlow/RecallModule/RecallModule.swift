import Foundation

protocol RecallModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
}
protocol RecallModuleDelegate: class {
  func didFinishRecall()
}

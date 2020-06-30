import Foundation

protocol RecallModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance])
}
protocol RecallModuleDelegate: class {
  func didFinishRecall()
}

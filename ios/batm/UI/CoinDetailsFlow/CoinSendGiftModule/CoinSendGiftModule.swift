import Foundation

protocol CoinSendGiftModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings)
}
protocol CoinSendGiftModuleDelegate: class {
  func didFinishCoinSendGift()
}

import Foundation

protocol CoinSendGiftModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
}
protocol CoinSendGiftModuleDelegate: class {
  func didFinishCoinSendGift()
}

import Foundation

protocol CoinSendGiftModule: class {
  func setup(coin: BTMCoin, coinBalance: CoinBalance, coinSettings: CoinSettings)
}
protocol CoinSendGiftModuleDelegate: class {
  func didFinishCoinSendGift()
}

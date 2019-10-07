import Foundation

protocol CoinSendGiftModule: class {
  func setup(with coin: BTMCoin)
  func setup(with coinBalance: CoinBalance)
}
protocol CoinSendGiftModuleDelegate: class {
  func didFinishCoinSendGift()
}

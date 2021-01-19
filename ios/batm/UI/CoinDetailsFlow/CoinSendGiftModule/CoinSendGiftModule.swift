import Foundation

protocol CoinSendGiftModule: class {
  func setupContact(_ contact: BContact)
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
}
protocol CoinSendGiftModuleDelegate: class {
  func didFinishCoinSendGift()
}

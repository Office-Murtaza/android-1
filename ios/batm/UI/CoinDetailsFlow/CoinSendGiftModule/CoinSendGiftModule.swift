import Foundation

protocol CoinSendGiftModule: AnyObject {
  func setupContact(_ contact: BContact)
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
}
protocol CoinSendGiftModuleDelegate: AnyObject {
  func didFinishCoinSendGift()
}

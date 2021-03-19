import Foundation

protocol CoinSellDetailsCurrentAddressModule: AnyObject {
  func setup(with details: SellDetailsForCurrentAddress)
}
protocol CoinSellDetailsCurrentAddressModuleDelegate: AnyObject {
  func didFinishCoinSellDetailsCurrentAddress()
}

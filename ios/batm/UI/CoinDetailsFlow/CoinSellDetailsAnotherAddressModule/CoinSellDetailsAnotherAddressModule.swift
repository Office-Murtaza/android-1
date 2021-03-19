import Foundation

protocol CoinSellDetailsAnotherAddressModule: AnyObject {
  func setup(with details: SellDetailsForAnotherAddress)
}
protocol CoinSellDetailsAnotherAddressModuleDelegate: AnyObject {
  func didFinishCoinSellDetailsAnotherAddress()
}

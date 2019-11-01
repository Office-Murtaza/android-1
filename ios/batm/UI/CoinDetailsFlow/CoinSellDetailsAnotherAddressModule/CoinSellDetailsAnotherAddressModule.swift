import Foundation

protocol CoinSellDetailsAnotherAddressModule: class {
  func setup(with details: SellDetailsForAnotherAddress)
}
protocol CoinSellDetailsAnotherAddressModuleDelegate: class {
  func didFinishCoinSellDetailsAnotherAddress()
}

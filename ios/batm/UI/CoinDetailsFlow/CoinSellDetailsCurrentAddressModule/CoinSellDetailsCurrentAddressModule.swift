import Foundation

protocol CoinSellDetailsCurrentAddressModule: class {
  func setup(with details: SellDetailsForCurrentAddress)
}
protocol CoinSellDetailsCurrentAddressModuleDelegate: class {
  func didFinishCoinSellDetailsCurrentAddress()
}

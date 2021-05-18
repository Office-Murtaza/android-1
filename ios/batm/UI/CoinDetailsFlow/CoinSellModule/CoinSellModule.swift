import Foundation

protocol CoinSellModule: AnyObject {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails, details: SellDetails)
}
protocol CoinSellModuleDelegate: AnyObject {
  func showSellDetailsForAnotherAddress(_ details: SellDetailsForAnotherAddress)
  func showSellDetailsForCurrentAddress(_ details: SellDetailsForCurrentAddress)
  func didFinishCoinSell()
}

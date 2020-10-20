import Foundation

protocol CoinSellModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails, details: SellDetails)
}
protocol CoinSellModuleDelegate: class {
  func showSellDetailsForAnotherAddress(_ details: SellDetailsForAnotherAddress)
  func showSellDetailsForCurrentAddress(_ details: SellDetailsForCurrentAddress)
  func didFinishCoinSell()
}

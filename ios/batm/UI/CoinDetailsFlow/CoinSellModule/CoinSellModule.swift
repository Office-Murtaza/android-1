import Foundation

protocol CoinSellModule: class {
  func setup(coin: BTMCoin, coinBalance: CoinBalance, coinSettings: CoinSettings, details: SellDetails)
}
protocol CoinSellModuleDelegate: class {
  func showSellDetailsForAnotherAddress(_ details: SellDetailsForAnotherAddress)
  func showSellDetailsForCurrentAddress(_ details: SellDetailsForCurrentAddress)
  func didFinishCoinSell()
}

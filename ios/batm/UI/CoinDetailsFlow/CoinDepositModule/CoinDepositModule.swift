import Foundation

protocol CoinDepositModule: class {
  func setup(coin: BTMCoin)
}
protocol CoinDepositModuleDelegate: class {
  func didFinishCoinDeposit()
}

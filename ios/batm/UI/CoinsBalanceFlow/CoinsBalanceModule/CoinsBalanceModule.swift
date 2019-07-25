import Foundation

protocol CoinsBalanceModule: class {
  func fetchCoinsBalance()
}
protocol CoinsBalanceModuleDelegate: class {
  func showFilterCoins(from module: CoinsBalanceModule)
}

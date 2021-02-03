import Foundation

protocol CoinWithdrawModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails)
}
protocol CoinWithdrawModuleDelegate: class {
    func didFinishCoinWithdraw(with transactionResult: String)
}

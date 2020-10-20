import Foundation

protocol CoinStakingModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails, stakeDetails: StakeDetails)
}
protocol CoinStakingModuleDelegate: class {
  func didFinishCoinStaking()
}

import Foundation

protocol CoinStakingModule: class {
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings, stakeDetails: StakeDetails)
}
protocol CoinStakingModuleDelegate: class {
  func didFinishCoinStaking()
}

import Foundation

protocol DealsModule: class {}
protocol DealsModuleDelegate: class {
    func didSelectStaking(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails, stakeDetails: StakeDetails)
    func didSelectSwap()
    func didSelectTransfer()
}

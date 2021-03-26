import Foundation

protocol CoinStakingModule: class {
    func setup()
}

protocol CoinStakingModuleDelegate: class {
    func didFinishCoinStaking()
}

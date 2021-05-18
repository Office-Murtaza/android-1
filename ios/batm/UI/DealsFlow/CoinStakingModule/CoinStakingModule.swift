import Foundation

protocol CoinStakingModule: AnyObject {
    func setup()
}

protocol CoinStakingModuleDelegate: AnyObject {
    func didFinishCoinStaking()
}

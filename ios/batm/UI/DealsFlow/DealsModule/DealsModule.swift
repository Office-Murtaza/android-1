import Foundation

protocol DealsModule: class {}
protocol DealsModuleDelegate: class {
    func didSelectStaking()
    func didSelectSwap()
    func didSelectTransfer()
    func didSelectedP2p()
}

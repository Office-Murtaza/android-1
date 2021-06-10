import Foundation

protocol DealsModule: AnyObject {}
protocol DealsModuleDelegate: AnyObject {
    func didSelectStaking()
    func didSelectSwap()
    func didSelectTransfer()
    func didSelectedP2p(trades: Trades, userId: String)
}

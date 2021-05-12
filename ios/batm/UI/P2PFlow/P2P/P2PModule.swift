import Foundation

protocol P2PModule: AnyObject {
    func setup(trades: Trades, userId: Int)
    func willHideModule()
}

protocol P2PModuleDelegate: AnyObject {
    
}

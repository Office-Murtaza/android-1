import Foundation

protocol P2PModule: AnyObject {
    func setup(trades: Trades, userId: Int)
}

protocol P2PModuleDelegate: AnyObject {
    
}

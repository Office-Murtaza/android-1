import Foundation

protocol P2PModule: AnyObject {
    func setup(trades: Trades, userId: String)
}

protocol P2PModuleDelegate: AnyObject {
    
}

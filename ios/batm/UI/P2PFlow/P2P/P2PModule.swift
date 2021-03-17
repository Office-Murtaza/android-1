import Foundation

protocol P2PModule: class {
    func setup(trades: Trades, userId: Int)
}

protocol P2PModuleDelegate: class {
    
}

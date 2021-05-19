import Foundation


enum P2POrderDetailsUpdateStatus: Int {
    case new = 1
    case canceled
    case doing
    case paid
    case released
    case disputing
    case solved
}

enum P2POrderDetailsActions {
    case update(status: P2POrderDetailsUpdateStatus, rate: Int)
    case cancel
}

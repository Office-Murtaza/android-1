import UIKit

enum TradeOrderStatus: Int {
    case new = 1
    case canceled = 2
    case doing = 3
    case paid = 4
    case released = 5
    case disputing = 6
    case solved = 7
    
    var title: String {
        switch self {
        case .new: return localize(L.P2p.Order.Status.new)
        case .canceled: return localize(L.P2p.Order.Status.canceled)
        case .doing: return localize(L.P2p.Order.Status.doing)
        case .paid: return localize(L.P2p.Order.Status.paid)
        case .released: return localize(L.P2p.Order.Status.released)
        case .disputing: return localize(L.P2p.Order.Status.disputing)
        case .solved: return localize(L.P2p.Order.Status.solved)
        }
    }
    
    var image: UIImage? {
        switch self {
        case .new: return UIImage(named: "p2p_order_status_new")
        case .canceled: return UIImage(named: "")
        case .doing: return UIImage(named: "p2p_order_status_doing")
        case .paid: return UIImage(named: "p2p_order_status_paid")
        case .released: return UIImage(named: "")
        case .disputing: return UIImage(named: "p2p_order_status_disputing")
        case .solved: return UIImage(named: "")
        }
    }
    
    
}

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
        case .new: return "New"
        case .canceled: return "Canceled"
        case .doing: return "Doing"
        case .paid: return "Paid"
        case .released: return "Released"
        case .disputing: return "Disputing"
        case .solved: return "Solved"
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

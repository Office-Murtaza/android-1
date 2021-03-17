import UIKit

enum TradeVerificationStatus: Int {
    case notVerified = 1
    case verified = 4
    case vipVerfified  = 7
    
    
    var image: UIImage? {
        switch self {
        case .notVerified: return UIImage(named: "p2p_not_verified")
        case .verified: return UIImage(named: "p2p_verified_trade")
        case .vipVerfified: return UIImage(named: "p2p_vip_verified")
        }
    }
    
    var status: String {
        switch self {
        case .notVerified: return "Not Verified"
        case .verified: return "Verified"
        case .vipVerfified: return "Vip Verified"
        }
    }
}

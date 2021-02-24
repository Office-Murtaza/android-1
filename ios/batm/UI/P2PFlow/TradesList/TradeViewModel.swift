import UIKit
import CoreLocation

enum TradePaymentMethods: Int {
    case cash = 1
    case payPal
    case venmo
    case cashApp
    case payoneer
    
    var image: UIImage {
        switch self {
        case .cash: return UIImage(named: "p2p_cash")!
        case .payPal: return UIImage(named: "p2p_pay_pal")!
        case .venmo: return UIImage(named: "p2p_venmo")!
        case .cashApp: return UIImage(named: "p2p_cash_app")!
        case .payoneer: return UIImage(named: "p2p_payoneer")!
        }
    }
}

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
}

class TradeViewModel {
    
    private let trade: Trade
    private let total: Double
    private let rate: Double
    
    var currentLocation: CLLocation?
    
    var coin: CustomCoinType? {
        return CustomCoinType(code: trade.coin ?? "BTC")
    }
    
    var price: String {
        return "$ \((trade.price ?? 0).coinFormatted)"
    }
    
    var markerPublicId: String {
        return trade.makerPublicId ?? ""
    }
    
    var limit: String {
        return "$ \((trade.minLimit ?? 0).coinFormatted) - $ \((trade.maxLimit ?? 0).coinFormatted)"
    }
    
    var paymentMethods: [UIImage]? {
        guard let methods = trade.paymentMethods else { return nil }
        let images =  methods
            .components(separatedBy: ",")
            .compactMap{ Int($0) }
            .compactMap{ TradePaymentMethods.init(rawValue: $0)?.image }
            
        return images
    }
    
    var tradeStatusImage: UIImage? {
        guard let status = trade.status else { return UIImage(named: "p2p_not_verified")}
        return TradeVerificationStatus(rawValue: status)?.image
    }
    
    var distanceInMiles: String? {
        guard let location = currentLocation,
              let latitude = trade.makerLatitude,
              let longitude = trade.makerLongitude  else { return nil }
        let markerLocation = CLLocation(latitude: latitude, longitude: longitude)
        let distance = (markerLocation.distance(from: location) * 0.000621371).rounded()
        return String(distance)
    }
    
    var isRateHidden: Bool {
        return (rate == 0 && total == 0)
    }
    
    var tradingRate: String {
        return rate.formatted(fractionPart: 1)
    }
    
    var totalTrades: String {
        return total.formatted(fractionPart: 1)
    }
    
    init(trade: Trade, totalTrades: Double, rate: Double) {
        self.trade = trade
        self.total = totalTrades
        self.rate = rate
    }
}

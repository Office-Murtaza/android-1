import UIKit

enum TradePaymentMethods: Int, CaseIterable {
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
    
    var title: String {
        switch self {
        case .cash: return localize(L.P2p.PaymentMethod.cash)
        case .payPal: return localize(L.P2p.PaymentMethod.paypal)
        case .venmo: return localize(L.P2p.PaymentMethod.venmo)
        case .cashApp: return localize(L.P2p.PaymentMethod.cashApp)
        case .payoneer: return localize(L.P2p.PaymentMethod.payoneer)
        }
    }
    
    init?(method: String) {
        switch method {
        case TradePaymentMethods.cash.title: self = .cash
        case TradePaymentMethods.payPal.title: self = .payPal
        case TradePaymentMethods.venmo.title: self = .venmo
        case TradePaymentMethods.cashApp.title: self = .cashApp
        case TradePaymentMethods.payoneer.title: self = .payoneer
        default: return nil
        }
    }
}

import Foundation


protocol Validator {
    func isValid() -> Bool
}

struct CoinsValidator: Validator {
    let coins: [CustomCoinType]
    let trade: Trade
    
    func isValid() -> Bool {
        guard let tradeCoin = CustomCoinType(code: trade.coin ?? "") else {
            return false
        }
        return coins.contains(tradeCoin)
    }
}

struct PaymentValidator: Validator {
    let trade: Trade
    let paymentMethods: [TradePaymentMethods]
    
    func isValid() -> Bool {
        guard let payments = trade.paymentMethods  else { return false }
        
        let tradePayments: [TradePaymentMethods] = payments.components(separatedBy: ",")
            .compactMap{ Int($0) }
            .compactMap{ TradePaymentMethods(rawValue: $0)}
        
        let paymentIntersection = Set(tradePayments).intersection(Set(paymentMethods))
        
        return paymentIntersection.isNotEmpty
    }
}

struct RangeValidator: Validator {
    let distance: Double
    let minRange: Int
    let maxRange: Int
    
    func isValid() -> Bool {
        return minRange..<maxRange ~= Int(distance)
    }
}


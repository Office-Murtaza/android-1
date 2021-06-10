import UIKit
import CoreLocation

class TradeViewModel {
    
    let trade: Trade
    private let total: Double
    private let rate: Double
    
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
    
    var distanceInMiles: String?
    
    var distance: Double?
    
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
    
   func update(location: CLLocation?) {
        guard let location = location,
              let latitude = trade.makerLatitude,
              let longitude = trade.makerLongitude  else { return }
        let markerLocation = CLLocation(latitude: latitude, longitude: longitude)
        distance = (markerLocation.distance(from: location) * 0.000621371).round(to: 2)
        distanceInMiles = String(distance ?? 0)
    }
    
    func isInclude(_ scope: FilterScopeModel) -> Bool {
        var validators = [Validator]()
        
        if scope.coins.isNotEmpty {
            validators.append(CoinsValidator(coins: scope.coins, trade: trade))
        }

        if scope.paymentMethods.isNotEmpty {
            validators.append(PaymentValidator(trade: trade, paymentMethods: scope.paymentMethods))
        }
        
        if scope.maxRange != 0 {
            validators.append(RangeValidator(distance: distance ?? 0,
                                             minRange: scope.minRange,
                                             maxRange: scope.maxRange))
        }
        
        for validator in validators {
            if validator.isValid() == false {
                return false
            }
        }

        return true
    }
}

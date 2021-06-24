import UIKit
import CoreLocation

class MyOrderViewModel {
    
   private(set) var order: Order
   private(set) var userId: String
   
  var currentSellBuyType: P2PSellBuyViewType {
    return (order.makerUserId == userId ? tradeType : tradeType?.reversed) ?? .buy
  }
    
  private var tradeType: P2PSellBuyViewType?
  
  init(order: Order, userId: String) {
        self.order = order
        self.userId = userId
    }
  
  func update(order: Order) {
    self.order = order
  }
  
  func upate(type: P2PSellBuyViewType) {
    tradeType = type
  }
    
    var price: String {
        let price = order.price ?? 0
        return "$ \(price.coinFormatted)"
    }
    
    var coin: CustomCoinType? {
        return CustomCoinType(code: order.coin ?? "BTC")
    }
    
    var orderStatus: TradeOrderStatus {
        return TradeOrderStatus(rawValue: order.status ?? 1) ?? .new
    }
    
    var paymentMethods: [UIImage]? {
        guard let methods = order.paymentMethods else { return nil }
        let images =  methods
            .components(separatedBy: ",")
            .compactMap{ Int($0) }
            .compactMap{ TradePaymentMethods.init(rawValue: $0)?.image }
            
        return images
    }
    
    var distanceInMiles: String?
        
    var distance: Double?
    
    func update(location: CLLocation?) {
           guard let location = location,
                 let latitude = order.makerLatitude,
                 let longitude = order.makerLongitude else { return }
        
           let markerLocation = CLLocation(latitude: latitude, longitude: longitude)
           distance = (markerLocation.distance(from: location) * 0.000621371).round(to: 2)
           distanceInMiles = String(distance ?? 0)
       }
    
    
    //MARK: - Crypto amount
    
    var cryptoAmountTitle: String {
      return localize(L.P2p.Crypto.Amount.title)
    }
    
    var cryptoAmount: String {
      return "\(order.cryptoAmount?.coinFormatted ?? "") \(order.coin ?? "")"
    }
    
    var fiatAmountTitle: String {
      return localize(L.P2p.Fiat.Amount.title)
    }
    
    var fiatAmount: String {
        return "$ \(order.fiatAmount?.coinFormatted ?? "")"
    }
  
  var isNeedPresentRateView: Bool {
    if order.status == OrderDetailsActionType.release.networkType {
      switch currentSellBuyType {
        case .sell: return order.makerRate == nil
        case .buy: return order.takerRate == nil
      }
    }
    return false
  }
  
  var makerId: String {
    return (order.makerUserId == userId ? order.takerPublicId : order.makerPublicId) ?? ""
  }
  
  var tradingRate: Double {
    return (order.makerUserId == userId ? order.takerTradingRate : order.makerTradingRate) ?? 0
  }
  
  var totalTrdades: Double {
    return (order.makerUserId == userId ? order.takerTotalTrades : order.makerTotalTrades) ?? 0
  }
  
  var terms: String {
    return order.terms ?? ""
  }

  var makerRate: String {
    return userId == order.takerUserId ? order.makerRate.toString() : order.takerRate.toString()
  }
  
  var partnerRate: String {
    return userId == order.takerUserId ? order.takerRate.toString() : order.makerRate.toString()
  }
  
}

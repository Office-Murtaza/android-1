import UIKit
import CoreLocation

class MyOrderViewModel {
    
   private(set) var order: Order
   
  var currentSellBuyType: P2PSellBuyViewType {
    return (order.takerId == order.makerUserId ? tradeType : tradeType?.reversed) ?? .buy
  }
    
  private var tradeType: P2PSellBuyViewType?
  
    init(order: Order) {
        self.order = order
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
           distance = (markerLocation.distance(from: location) * 0.000621371).rounded()
           distanceInMiles = String(distance ?? 0)
       }
    
    
    //MARK: - Crypto amount
    
    var cryptoAmountTitle: String {
      return localize(L.P2p.Crypto.Amount.title)
    }
    
    var cryptoAmount: String {
        return order.cryptoAmount?.coinFormatted ?? ""
    }
    
    var fiatAmountTitle: String {
      return localize(L.P2p.Fiat.Amount.title)
    }
    
    var fiatAmount: String {
        return order.fiatAmount?.coinFormatted ?? ""
    }

}

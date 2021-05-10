import UIKit

class MyOpenOrdersCellViewModel {
    
    let order: Order
    
    init(order: Order) {
        self.order = order
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
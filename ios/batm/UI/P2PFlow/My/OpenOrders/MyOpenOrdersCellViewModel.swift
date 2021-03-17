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
        return "Crypto amount"
    }
    
    var cryptoAmount: String {
        return order.cryptoAmount?.coinFormatted ?? ""
    }
    
    var fiatAmountTitle: String {
        return "Fiat amount"
    }
    
    var fiatAmount: String {
        return order.fiatAmount?.coinFormatted ?? ""
    }
    
//    var sellbuyType: P2PSellBuyViewType {
//        return trade.type == 1 ? .buy : .sell
//    }
    
}

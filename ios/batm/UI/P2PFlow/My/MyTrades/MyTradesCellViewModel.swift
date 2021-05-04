import UIKit

class MyTradesCellViewModel {
    let trade: Trade
    init(trade: Trade) {
        self.trade = trade
    }
    var coin: CustomCoinType? {
        return CustomCoinType(code:trade.coin ?? "BTC")
    }
    
    var price: String {
        return "$ \((trade.price ?? 0).coinFormatted)"
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
    
    var sellbuyType: P2PSellBuyViewType {
        return trade.type == 1 ? .buy : .sell
    }
}

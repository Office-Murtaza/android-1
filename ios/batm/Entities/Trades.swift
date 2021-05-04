import Foundation


struct Trade: Equatable {
    var id: String?
    var type: Int?
    var coin: String?
    var status: Int?
    var timestamp: Int?
    var price: Double?
    var minLimit: Double?
    var maxLimit: Double?
    var paymentMethods: String?
    var terms: String?
    var openOrders: Int?
    var makerUserId: Int?
    var makerPublicId: String?
    var makerLatitude: Double?
    var makerLongitude: Double?
    var makerTotalTrades: Double?
    var makerTradingRate: Double?
}

struct Order: Equatable {
    var id: String?
    var tradeId: String?
    var coin: String?
    var status: Int?
    var timestamp: Int?
    var price: Double?
    var cryptoAmount: Double?
    var fiatAmount: Double?
    var terms: String?
    var makerUserId: Int?
    var makerPublicId: String?
    var makerStatus: Int?
    var makerTotalTrades: Double?
    var makerTradingRate: Double?
    var takerId: Int?
    var takerPublicId: String?
    var takerLatitude: Double?
    var takerLongitude: Double?
    var takerTotalTrades: Double?
    var takerTradingRate: Double?
    var paymentMethods: String?
}

struct Trades: Equatable {
    var makerPublicId: String
    var makerStatus: Int
    var makerTotalTrades: Double?
    var makerTradingRate: Double?
    var trades: [Trade]
    var orders: [Order]
}

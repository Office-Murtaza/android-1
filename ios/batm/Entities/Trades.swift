import Foundation


struct Trade: Equatable {
    var id: Int
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
    var makerId: Int?
    var makerPublicId: String?
    var makerLatitude: Double?
    var makerLongitude: Double?
    var makerTotalTrades: Double?
    var makerTradingRate: Double?
}

struct Order: Equatable {
    var id: Int
    var tradeId: Int
    var coin: String
    var status: Int
    var createDate: String
    var price: Double
    var cryptoAmount: Double
    var fiatAmount: Double
    var terms: String
    var makerId: Int
    var makerPublicId: String
    var makerLatitude: Double
    var makerLongitude: Double
    var makerTotalTrades: Double
    var makerTradingRate: Double
    var takerId: Int
    var takerPublicId: String
    var takerLatitude: Double
    var takerLongitude: Double
    var takerTotalTrades: Double
    var takerTradingRate: Double
}

struct Trades: Equatable {
    var publicId: String
    var status: Int
    var totalTrades: Double
    var tradingRate: Double
    var trades: [Trade]
//    var orders: [Order]
}

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
  
  static var empty = Trade(id: nil,
                           type: nil,
                           coin: nil,
                           status: nil,
                           timestamp: nil,
                           price: nil,
                           minLimit: nil,
                           maxLimit: nil,
                           paymentMethods: nil,
                           terms: nil,
                           openOrders: nil,
                           makerUserId: nil,
                           makerPublicId: nil,
                           makerLatitude: nil,
                           makerLongitude: nil,
                           makerTotalTrades: nil,
                           makerTradingRate: nil)
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
    var takerPublicId: String?
    var takerLatitude: Double?
    var takerLongitude: Double?
    var takerTotalTrades: Double?
    var takerTradingRate: Double?
    var paymentMethods: String?
    var makerLatitude: Double?
    var makerLongitude: Double?
    var takerUserId: Int?
    var makerRate: Int?
    var takerRate: Int?
    
    static var empty = Order(id: nil,
                             tradeId: nil,
                             coin: nil,
                             status: nil,
                             timestamp: nil,
                             price: nil,
                             cryptoAmount: nil,
                             fiatAmount: nil,
                             terms: nil,
                             makerUserId: nil,
                             makerPublicId: nil,
                             makerStatus: nil,
                             makerTotalTrades: nil,
                             makerTradingRate: nil,
                             takerPublicId: nil,
                             takerLatitude: nil,
                             takerLongitude: nil,
                             takerTotalTrades: nil,
                             takerTradingRate: nil,
                             paymentMethods: nil,
                             makerLatitude: nil,
                             makerLongitude: nil,
                             takerUserId: nil)
    
}

struct Trades: Equatable {
    var makerPublicId: String
    var makerStatus: Int
    var makerTotalTrades: Double?
    var makerTradingRate: Double?
    var trades: [Trade]
    var orders: [Order]
}

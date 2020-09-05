import Foundation

enum TradeType {
  case unknown
  case buy
  case sell
  
  var verboseValue: String {
    switch self {
    case .unknown: return localize(L.CoinDetails.unknown)
    case .buy: return localize(L.Trades.buy)
    case .sell: return localize(L.Trades.sell)
    }
  }
  
  var rawValue: Int {
    switch self {
    case .unknown: return 0
    case .buy: return 1
    case .sell: return 2
    }
  }
  
  init(rawValue: Int) {
    switch rawValue {
    case 0: self = .unknown
    case 1: self = .buy
    case 2: self = .sell
    default: self = .unknown
    }
  }
}

struct BuySellTrade: Equatable {
  var id: Int
  var type: TradeType
  var username: String
  var tradeCount: Int
  var tradeRate: Double
  var distance: Int?
  var paymentMethod: String
  var price: Double
  var minLimit: Int
  var maxLimit: Int
  var terms: String
  
  var formattedLimits: String {
    return "\(minLimit) - \(maxLimit)".withDollarSign
  }
  
  var formattedTradeCount: String {
    switch tradeCount {
    case ..<100: return "\(tradeCount)"
    case 100..<1000: return "\(tradeCount / 100 * 100)+"
    case 1000..<10000: return "\(tradeCount / 1000 * 1000)+"
    case 10000..<100000: return "\(tradeCount / 10000 * 10000)+"
    case 100000...: return "\(tradeCount / 100000 * 100000)+"
    default: return "\(tradeCount)"
    }
  }
  
  var userStats: String {
    var formattedDistance = ""
    if let distance = distance {
      formattedDistance = ", \(distance)km"
    }
    
    return "(\(formattedTradeCount), \(tradeRate)\(formattedDistance))"
  }
  
  var userInfo: String {
    return "\(username) \(userStats)"
  }
}

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
  var index: Int
  var username: String
  var tradeCount: Int
  var tradeRate: Double
  var distance: Int?
  var paymentMethod: String
  var price: Double
  var minLimit: Int
  var maxLimit: Int
  var terms: String
}

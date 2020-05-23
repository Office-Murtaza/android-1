import Foundation

struct BuySellTrades: Equatable {
  var total: Int
  var trades: [BuySellTrade]
}

extension BuySellTrades {
  static var empty: BuySellTrades {
    return BuySellTrades(total: 0, trades: [])
  }
}

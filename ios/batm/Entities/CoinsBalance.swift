import Foundation

struct CoinsBalance: Equatable {
  let totalBalance: Double
  var coins: [CoinBalance]
  
  static var empty: CoinsBalance {
    return CoinsBalance(totalBalance: 0, coins: [])
  }
}


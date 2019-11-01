import Foundation

struct Transactions: Equatable {
  var total: Int
  var transactions: [Transaction]
}

extension Transactions {
  static var empty: Transactions {
    return Transactions(total: 0, transactions: [])
  }
}

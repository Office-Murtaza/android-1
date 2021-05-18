import Foundation

struct Transactions: Equatable {
    var total: Int
    var transactions: [TransactionDetails]
}

extension Transactions {
    static var empty: Transactions {
        return Transactions(total: 0, transactions: [])
    }
}

import Foundation

struct Utxo: Equatable {
  var txid: String
  var vout: Int
  var value: String
  var address: String
  var path: String
}

import Foundation

struct BTMTronBlockHeader: Equatable {
  var number: Int
  var txTrieRoot: String
  var witnessAddress: String
  var parentHash: String
  var version: Int
  var timestamp: Int
}

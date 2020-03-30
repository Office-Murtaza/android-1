import Foundation
import TrustWalletCore

struct BTMCoin: Equatable {
  let type: CoinType
  let privateKey: String
  let publicKey: String
  let isVisible: Bool
  let index: Int
  
  init(type: CoinType,
       privateKey: String,
       publicKey: String,
       isVisible: Bool = true,
       index: Int = 0) {
    self.type = type
    self.privateKey = privateKey
    self.publicKey = publicKey
    self.isVisible = isVisible
    self.index = index
  }
  
  func transactionFee(fee: Double) -> Int64 {
    return Int64(fee * Double(type.unit))
  }
}

extension BTMCoin: Comparable {
  static func < (lhs: BTMCoin, rhs: BTMCoin) -> Bool {
    if lhs.index == rhs.index {
      return lhs.type.verboseValue < rhs.type.verboseValue
    }
    
    return lhs.index < rhs.index
  }
}

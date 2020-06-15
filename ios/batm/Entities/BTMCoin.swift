import Foundation
import TrustWalletCore

struct BTMCoin: Equatable {
  let type: CustomCoinType
  let privateKey: String
  let address: String
  let isVisible: Bool
  let index: Int
  
  init(type: CustomCoinType,
       privateKey: String,
       address: String,
       isVisible: Bool = true,
       index: Int = 0) {
    self.type = type
    self.privateKey = privateKey
    self.address = address
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

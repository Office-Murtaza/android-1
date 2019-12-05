import Foundation
import TrustWalletCore

struct BTMCoin: Equatable {
  let type: CoinType
  let privateKey: String
  let publicKey: String
  let isVisible: Bool
  let index: Int
  let fee: Double
  let gasPrice: Int
  let gasLimit: Int
  
  init(type: CoinType,
       privateKey: String,
       publicKey: String,
       isVisible: Bool = true,
       index: Int = 0,
       fee: Double = 0,
       gasPrice: Int = 0,
       gasLimit: Int = 0) {
    self.type = type
    self.privateKey = privateKey
    self.publicKey = publicKey
    self.isVisible = isVisible
    self.index = index
    self.fee = fee
    self.gasPrice = gasPrice
    self.gasLimit = gasLimit
  }
  
  var transactionFee: Int64 {
    return Int64(fee * Double(type.unit))
  }
  
  var feeInUnit: Int {
    switch type {
    // fee = feePerByte, 1000 - max amount of bytes per transaction
    case .bitcoin, .bitcoinCash, .litecoin: return Int(fee * 1000 * Double(type.unit))
    case .ethereum: return gasLimit * gasPrice
    default: return Int(fee * Double(type.unit))
    }
  }
  
  var feeInCoin: Double {
    return Double(feeInUnit) / Double(type.unit)
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

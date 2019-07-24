import Foundation
import TrustWalletCore

struct CoinAddress {
  let type: CoinType
  let address: String
  
  init(type: CoinType, address: String) {
    self.type = type
    self.address = address
  }
  
  init(coin: BTMCoin) {
    self.type = coin.type
    self.address = coin.publicKey
  }
}

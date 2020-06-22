import Foundation
import TrustWalletCore

struct CoinAddress {
  let type: CustomCoinType
  let address: String
  
  init(type: CustomCoinType, address: String) {
    self.type = type
    self.address = address
  }
  
  init(coin: BTMCoin) {
    self.type = coin.type
    self.address = coin.address
  }
}

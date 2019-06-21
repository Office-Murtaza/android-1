import Foundation
import ObjectMapper
import TrustWalletCore

struct BTMWallet {
  let seedPhrase: String
  let coinAddresses: [CoinAddress]
}

struct CoinAddress: ImmutableMappable {
  let type: CoinType
  let address: String
  
  init(type: CoinType, address: String) {
    self.type = type
    self.address = address
  }
  
  init(map: Map) throws {
    fatalError("Doesn't support such mapping")
  }
  
  func mapping(map: Map) {
    type.code >>> map["coinCode"]
    address >>> map["publicKey"]
  }
}

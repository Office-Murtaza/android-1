import ObjectMapper
import TrustWalletCore

extension CoinFee: ImmutableMappable {
  init(map: Map) throws {
    let code: String = try map.value("code")
    
    guard let mappedType = CoinType(code: code) else {
      throw ObjectMapperError.couldNotMap
    }
    
    type = mappedType
    fee = try? map.value("fee")
    gasPrice = try? map.value("gasPrice")
    gasLimit = try? map.value("gasLimit")
  }
}

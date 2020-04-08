import ObjectMapper
import TrustWalletCore

extension CoinSettings: ImmutableMappable {
  init(map: Map) throws {
    let code: String = try map.value("code")
    
    guard let mappedType = CoinType(code: code) else {
      throw ObjectMapperError.couldNotMap
    }
    
    type = mappedType
    txFee = try map.value("txFee")
    byteFee = try? map.value("byteFee")
    gasPrice = try? map.value("gasPrice")
    gasLimit = try? map.value("gasLimit")
    profitC2C = try map.value("profitC2C")
    serverWalletAddress = try map.value("serverWalletAddress")
  }
}

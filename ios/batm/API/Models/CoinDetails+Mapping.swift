import ObjectMapper
import TrustWalletCore

extension CoinDetails: ImmutableMappable {
  init(map: Map) throws {
    let code: String = try map.value("code")
    
    guard
      let mappedType = CustomCoinType(code: code),
      let txFee = Decimal(string: try map.value("txFeeStr")),
      let platformSwapFee = Decimal(string: try map.value("platformSwapFeeStr"))
    else {
      throw ObjectMapperError.couldNotMap
    }
    
    type = mappedType
    self.txFee = txFee
    byteFee = try map.value("byteFee")
    scale = try map.value("scale")
    gasPrice = try map.value("gasPrice")
    gasLimit = try map.value("gasLimit")
    self.platformSwapFee = platformSwapFee
    platformTradeFee = Decimal(string: try map.value("platformTradeFeeStr"))
    walletAddress = try map.value("walletAddress")
    contractAddress = try map.value("contractAddress")
    if map.JSON.keys.contains("convertedTxFeeStr") {
        convertedTxFee = Decimal(string: try map.value("convertedTxFeeStr"))
    }
  }
}

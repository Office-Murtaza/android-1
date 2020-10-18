import ObjectMapper
import TrustWalletCore

extension CoinDetails: ImmutableMappable {
  init(map: Map) throws {
    let code: String = try map.value("code")
    
    guard
      let mappedType = CustomCoinType(code: code),
      let txFee = Decimal(string: try map.value("txFeeStr")),
      let profitExchange = Decimal(string: try map.value("profitExchangeStr"))
    else {
      throw ObjectMapperError.couldNotMap
    }
    
    let recallFee: String? = try map.value("recallFeeStr")
    
    type = mappedType
    self.txFee = txFee
    self.recallFee = recallFee.flatMap { Decimal(string: $0) }
    byteFee = try map.value("byteFee")
    scale = try map.value("scale")
    gasPrice = try map.value("gasPrice")
    gasLimit = try map.value("gasLimit")
    self.profitExchange = profitExchange
    walletAddress = try map.value("walletAddress")
    contractAddress = try map.value("contractAddress")
  }
}

import ObjectMapper
import TrustWalletCore

extension CoinSettings: ImmutableMappable {
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
    let byteFee: String? = try map.value("byteFeeStr")
    
    type = mappedType
    self.txFee = txFee
    self.recallFee = recallFee.flatMap { Decimal(string: $0) }
    self.byteFee = byteFee.flatMap { Decimal(string: $0) }
    gasPrice = try map.value("gasPrice")
    gasLimit = try map.value("gasLimit")
    self.profitExchange = profitExchange
    walletAddress = try map.value("walletAddress")
    contractAddress = try map.value("contractAddress")
  }
}

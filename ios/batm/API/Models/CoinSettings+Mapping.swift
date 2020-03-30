import ObjectMapper
import TrustWalletCore

extension CoinSettings: ImmutableMappable {
  init(map: Map) throws {
    txFee = try map.value("txFee")
    byteFee = try? map.value("byteFee")
    gasPrice = try? map.value("gasPrice")
    gasLimit = try? map.value("gasLimit")
    profitC2C = try map.value("profitC2C")
  }
}

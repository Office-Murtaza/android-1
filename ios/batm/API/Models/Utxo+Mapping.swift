import ObjectMapper

extension Utxo: ImmutableMappable {
  init(map: Map) throws {
    txid = try map.value("txid")
    vout = try map.value("vout")
    value = try map.value("value")
    address = try map.value("address")
    path = try map.value("path")
  }
}

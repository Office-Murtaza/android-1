import ObjectMapper

extension Utxos: ImmutableMappable {
  init(map: Map) throws {
    utxos = try map.value("utxos")
  }
}

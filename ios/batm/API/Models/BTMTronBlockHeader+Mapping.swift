import ObjectMapper

extension BTMTronBlockHeader: ImmutableMappable {
  init(map: Map) throws {
    number = try map.value("block_header.raw_data.number")
    txTrieRoot = try map.value("block_header.raw_data.txTrieRoot")
    witnessAddress = try map.value("block_header.raw_data.witness_address")
    parentHash = try map.value("block_header.raw_data.parentHash")
    version = try map.value("block_header.raw_data.version")
    timestamp = try map.value("block_header.raw_data.timestamp")
  }
}


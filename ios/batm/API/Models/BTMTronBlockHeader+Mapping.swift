import ObjectMapper

extension BTMTronBlockHeader: ImmutableMappable {
  init(map: Map) throws {
    number = try map.value("blockHeader.raw_data.number")
    txTrieRoot = try map.value("blockHeader.raw_data.txTrieRoot")
    witnessAddress = try map.value("blockHeader.raw_data.witness_address")
    parentHash = try map.value("blockHeader.raw_data.parentHash")
    version = try map.value("blockHeader.raw_data.version")
    timestamp = try map.value("blockHeader.raw_data.timestamp")
  }
}


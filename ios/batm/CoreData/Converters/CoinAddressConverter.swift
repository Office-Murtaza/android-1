import Foundation
import CoreData
import TrustWalletCore

class CoinAddressConverter: Converter<CoinAddressRecord, CoinAddress> {
  override func convert(model: CoinAddressRecord) throws -> CoinAddress {
    guard let type = CoinType(rawValue: UInt32(model.type)) else {
      throw ConverterErrors.error("Couldn't map CoinType from CoinAddressRecord to CoinAddress")
    }
    return CoinAddress(type: type, address: model.address)
  }
}

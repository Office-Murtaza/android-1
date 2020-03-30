import Foundation
import CoreData
import TrustWalletCore

class BTMCoinConverter: Converter<BTMCoinRecord, BTMCoin> {
  override func convert(model: BTMCoinRecord) throws -> BTMCoin {
    guard let type = CoinType(rawValue: UInt32(model.type)) else {
      throw ConverterErrors.error("Couldn't map CoinType from BTMCoinRecord to BTMCoin")
    }
    return BTMCoin(type: type,
                   privateKey: model.privateKey,
                   publicKey: model.publicKey,
                   isVisible: model.visible,
                   index: Int(model.index))
  }
}

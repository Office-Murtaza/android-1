import Foundation
import CoreData
import TrustWalletCore

class BTMCoinConverter: Converter<BTMCoinRecord, BTMCoin> {
  override func convert(model: BTMCoinRecord) throws -> BTMCoin {
    guard let type = CustomCoinType(code: model.type) else {
      throw ConverterErrors.error("Couldn't map CustomCoinType from BTMCoinRecord to BTMCoin")
    }
    return BTMCoin(type: type,
                   privateKey: model.privateKey,
                   address: model.address,
                   isVisible: model.visible,
                   index: Int(model.index))
  }
}

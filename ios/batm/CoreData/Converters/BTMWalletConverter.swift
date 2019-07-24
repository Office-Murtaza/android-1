import Foundation
import CoreData
import TrustWalletCore

class BTMWalletConverter: Converter<BTMWalletRecord, BTMWallet> {
  
  let coinConverter = BTMCoinConverter()
  
  override func convert(model: BTMWalletRecord) throws -> BTMWallet {
    let coins = try model.coins.map { try coinConverter.convert(model: $0) }
    return BTMWallet(seedPhrase: model.seedPhrase, coins: coins)
  }
}

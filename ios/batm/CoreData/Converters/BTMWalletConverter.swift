import Foundation
import CoreData
import TrustWalletCore

class BTMWalletConverter: Converter<BTMWalletRecord, BTMWallet> {
  
  let coinAddressConverter = CoinAddressConverter()
  
  override func convert(model: BTMWalletRecord) throws -> BTMWallet {
    let coinAddresses = try model.coinAddresses.map { try coinAddressConverter.convert(model: $0) }
    return BTMWallet(seedPhrase: model.seedPhrase, coinAddresses: coinAddresses)
  }
}

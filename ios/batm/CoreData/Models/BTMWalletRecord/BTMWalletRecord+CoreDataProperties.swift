import Foundation
import CoreData

extension BTMWalletRecord: ActiveRecord {
  
  @nonobjc public class func fetchRequest() -> NSFetchRequest<BTMWalletRecord> {
    return NSFetchRequest<BTMWalletRecord>(entityName: "BTMWalletRecord")
  }
  
  @NSManaged public var seedPhrase: String
  @NSManaged public var coinAddresses: Set<CoinAddressRecord>
  
}

extension ActiveRecord where Self: BTMWalletRecord {
  @discardableResult
  static func findOrCreate(in context: NSManagedObjectContext, wallet: BTMWallet) throws -> Self {
    let predicate = NSPredicate(format: "seedPhrase == %@", wallet.seedPhrase)
    let element = try fetchFirstOrCreate(matching: predicate, in: context)
    element.seedPhrase = wallet.seedPhrase
    element.coinAddresses.forEach { context.delete($0) }
    element.coinAddresses = try Set(wallet.coinAddresses.map {
      try CoinAddressRecord.create(in: context, coinAddress: $0)
    })
    return element
  }
}

import Foundation
import CoreData

extension CoinAddressRecord: ActiveRecord {
  
  @nonobjc public class func fetchRequest() -> NSFetchRequest<CoinAddressRecord> {
    return NSFetchRequest<CoinAddressRecord>(entityName: "CoinAddressRecord")
  }
  
  @NSManaged public var type: Int32
  @NSManaged public var address: String
  
}

extension ActiveRecord where Self: CoinAddressRecord {
  @discardableResult
  static func create(in context: NSManagedObjectContext, coinAddress: CoinAddress) throws -> Self {
    let element = try create(in: context)
    element.type = Int32(coinAddress.type.rawValue)
    element.address = coinAddress.address
    return element
  }
}

import Foundation
import CoreData

extension BTMCoinRecord: ActiveRecord {
  
  @nonobjc public class func fetchRequest() -> NSFetchRequest<BTMCoinRecord> {
    return NSFetchRequest<BTMCoinRecord>(entityName: "BTMCoinRecord")
  }
  
  @NSManaged public var type: Int32
  @NSManaged public var privateKey: String
  @NSManaged public var publicKey: String
  @NSManaged public var visible: Bool
  @NSManaged public var index: Int32
  
}

extension ActiveRecord where Self: BTMCoinRecord {
  @discardableResult
  static func create(in context: NSManagedObjectContext, coin: BTMCoin) throws -> Self {
    let element = try create(in: context)
    element.type = Int32(coin.type.rawValue)
    element.privateKey = coin.privateKey
    element.publicKey = coin.publicKey
    element.visible = coin.isVisible
    element.index = Int32(coin.index)
    return element
  }
}

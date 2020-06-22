import Foundation
import CoreData

extension BTMCoinRecord: ActiveRecord {
  
  @nonobjc public class func fetchRequest() -> NSFetchRequest<BTMCoinRecord> {
    return NSFetchRequest<BTMCoinRecord>(entityName: "BTMCoinRecord")
  }
  
  @NSManaged public var type: String
  @NSManaged public var privateKey: String
  @NSManaged public var address: String
  @NSManaged public var visible: Bool
  @NSManaged public var index: Int32
  
}

extension ActiveRecord where Self: BTMCoinRecord {
  @discardableResult
  static func create(in context: NSManagedObjectContext, coin: BTMCoin) throws -> Self {
    let element = try create(in: context)
    element.type = coin.type.code
    element.privateKey = coin.privateKey
    element.address = coin.address
    element.visible = coin.isVisible
    element.index = Int32(coin.index)
    return element
  }
}

import Foundation
import CoreData

extension AccountRecord: ActiveRecord {
  
  @nonobjc public class func fetchRequest() -> NSFetchRequest<AccountRecord> {
    return NSFetchRequest<AccountRecord>(entityName: "AccountRecord")
  }
  
  @NSManaged public var userId: String
  @NSManaged public var accessToken: String
  @NSManaged public var refreshToken: String
  @NSManaged public var expires: Date
  
}

extension ActiveRecord where Self: AccountRecord {
  @discardableResult
  static func findOrCreate(in context: NSManagedObjectContext, account: Account) throws -> Self {
    let element = try fetchFirstOrCreate(in: context)
    element.userId = account.userId
    element.accessToken = account.accessToken
    element.refreshToken = account.refreshToken
    element.expires = account.expires
    return element
  }
}

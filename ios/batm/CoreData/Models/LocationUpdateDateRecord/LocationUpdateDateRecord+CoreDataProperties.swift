import Foundation
import CoreData

extension LocationUpdateDateRecord: ActiveRecord {
  
  @nonobjc public class func fetchRequest() -> NSFetchRequest<LocationUpdateDateRecord> {
    return NSFetchRequest<LocationUpdateDateRecord>(entityName: "LocationUpdateDateRecord")
  }
  
  @NSManaged public var updateDate: Date
  
}

extension ActiveRecord where Self: LocationUpdateDateRecord {
  @discardableResult
  static func findOrCreate(in context: NSManagedObjectContext, updateDate: Date) throws -> Self {
    let element = try fetchFirstOrCreate(in: context)
    element.updateDate = updateDate
    return element
  }
}

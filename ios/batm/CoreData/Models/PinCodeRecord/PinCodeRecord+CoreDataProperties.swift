import Foundation
import CoreData

extension PinCodeRecord: ActiveRecord {
  
  @nonobjc public class func fetchRequest() -> NSFetchRequest<PinCodeRecord> {
    return NSFetchRequest<PinCodeRecord>(entityName: "PinCodeRecord")
  }
  
  @NSManaged public var pinCode: String
  
}

extension ActiveRecord where Self: PinCodeRecord {
  @discardableResult
  static func findOrCreate(in context: NSManagedObjectContext, pinCode: String) throws -> Self {
    let predicate = NSPredicate(format: "pinCode == %@", pinCode)
    let element = try fetchFirstOrCreate(matching: predicate, in: context)
    element.pinCode = pinCode
    return element
  }
}

import Foundation
import CoreData

struct ObjectId<T: NSManagedObject> {
  let raw: NSManagedObjectID
}

protocol ActiveRecord {}

protocol UniqueIdentifiable: AnyObject {
  associatedtype UidType: CVarArg, Equatable
  
  var uid: UidType { get set }
}

extension UniqueIdentifiable {
  static func predicate(for uid: UidType) -> NSPredicate {
    return NSPredicate(format: "uid == %@", (uid as? NSNumber) ?? uid)
  }
}

extension ActiveRecord where Self: NSManagedObject {
  typealias Identifier = ObjectId<Self>
  
  var identifier: Identifier {
    return ObjectId(raw: objectID)
  }
  
  static func builder() -> FetchRequestBuilder<Self> {
    return FetchRequestBuilder<Self>()
  }
  
  static func fetch(_ request: NSFetchRequest<Self>, in context: NSManagedObjectContext) throws -> [Self] {
    return try context.fetch(request)
  }
  
  static func create(in context: NSManagedObjectContext) throws -> Self {
    return Self(context: context)
  }
  
  static func fetch(matching predicate: NSPredicate? = nil,
                    sortedBy descriptors: [NSSortDescriptor]? = nil,
                    limit: Int = 0,
                    in context: NSManagedObjectContext) throws -> [Self] {
    let request = builder()
      .matching(predicate)
      .sorted(by: descriptors)
      .limit(to: limit)
      .build()
    return try context.fetch(request)
  }
  
  static func count(matching predicate: NSPredicate? = nil,
                    in context: NSManagedObjectContext) throws -> Int {
    let request = builder()
      .matching(predicate)
      .build()
    return try context.count(for: request)
  }
  
  static func fetch<V>(matching predicate: NSPredicate? = nil,
                       sortedBy key: KeyPath<Self, V>,
                       ascending: Bool,
                       limit: Int = 0,
                       in context: NSManagedObjectContext) throws -> [Self] {
    let request = builder()
      .matching(predicate)
      .sorted(by: key, ascending: ascending)
      .limit(to: limit)
      .build()
    return try context.fetch(request)
  }
  
  static func fetchFirst(matching predicate: NSPredicate? = nil,
                         sortedBy descriptors: [NSSortDescriptor]? = nil,
                         in context: NSManagedObjectContext) throws -> Self? {
    return try fetch(matching: predicate, sortedBy: descriptors, limit: 1, in: context).first
  }
  
  static func fetchAndDelete(matching predicate: NSPredicate? = nil,
                             in context: NSManagedObjectContext) throws {
    let objects = try fetch(matching: predicate, in: context)
    objects.forEach { context.delete($0) }
  }
  
  static func fetchFirst<V>(matching predicate: NSPredicate? = nil,
                            sortedBy key: KeyPath<Self, V>,
                            ascending: Bool,
                            in context: NSManagedObjectContext) throws -> Self? {
    return try fetch(matching: predicate, sortedBy: key, ascending: ascending, limit: 1, in: context).first
  }
  
  static func fetchFirstOrCreate(matching predicate: NSPredicate? = nil,
                                 in context: NSManagedObjectContext) throws -> Self {
    return try fetchFirst(matching: predicate, in: context) ?? Self(context: context)
  }
  
  static func clear(matching predicate: NSPredicate? = nil,
                    in context: NSManagedObjectContext) throws {
    let deleteRequest = NSBatchDeleteRequest.request(with: builder().matching(predicate).build())
    try context.execute(deleteRequest)
  }
}

extension ActiveRecord where Self: NSManagedObject & UniqueIdentifiable {
  
  static func fetch(with uid: UidType, in context: NSManagedObjectContext) throws -> Self? {
    return try fetchFirst(matching: predicate(for: uid), in: context)
  }
  
  static func delete(with uid: UidType, in context: NSManagedObjectContext) throws {
    let model = try fetchFirst(matching: predicate(for: uid), in: context)
    model.map { context.delete($0) }
  }
  
  static func fetchOrCreate(with uid: UidType, in context: NSManagedObjectContext) throws -> Self {
    let element = try fetchFirstOrCreate(matching: predicate(for: uid), in: context)
    element.uid = uid
    return element
  }
}

fileprivate extension NSBatchDeleteRequest {
  
  static func request<T: NSFetchRequestResult>(with fetchRequest: NSFetchRequest<T>) -> NSBatchDeleteRequest {
    //  swiftlint:disable force_cast
    return NSBatchDeleteRequest(fetchRequest: fetchRequest as! NSFetchRequest<NSFetchRequestResult>)
    //  swiftlint:enable force_cast
  }
}

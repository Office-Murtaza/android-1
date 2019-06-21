import Foundation
import CoreData

class FRCBuilder<E> where E: NSFetchRequestResult {
  private var request = FetchRequestBuilder<E>()
  private var context: NSManagedObjectContext!
  private var sectionKeyPath: String?
  private var cacheName: String?
  private var searchPredicate: NSPredicate?
  
  @discardableResult
  func matching(_ predicate: NSPredicate?) -> Self {
    request = request.matching(predicate)
    return self
  }
  
  @discardableResult
  func sorted(by sortDescriptors: [NSSortDescriptor]?) -> Self {
    request = request.sorted(by: sortDescriptors)
    return self
  }
  
  @discardableResult
  func sorted<V>(by key: KeyPath<E, V>, ascending: Bool) -> Self {
    request = request.sorted(by: key, ascending: ascending)
    return self
  }
  
  @discardableResult
  func limit(to limit: Int) -> Self {
    request = request.limit(to: limit)
    return self
  }
  
  @discardableResult
  func batch(limit: Int) -> Self {
    request = request.batch(limit: limit)
    return self
  }
  
  @discardableResult
  func prefetch(relationship: String) -> Self {
    request = request.prefetch(relationship: relationship)
    return self
  }
  
  @discardableResult
  func inContext(_ context: NSManagedObjectContext) -> Self {
    self.context = context
    return self
  }
  
  @discardableResult
  func group(by sectionKeyPath: String) -> Self {
    self.sectionKeyPath = sectionKeyPath
    return self
  }
  
  @discardableResult
  func with(cache: String) -> Self {
    self.cacheName = cache
    return self
  }
  
  @discardableResult
  func search(by predicate: NSPredicate?) -> Self {
    request = request.search(by: predicate)
    return self
  }
  
  func build() -> NSFetchedResultsController<E> {
    return NSFetchedResultsController(fetchRequest: request.build(),
                                      managedObjectContext: context,
                                      sectionNameKeyPath: sectionKeyPath,
                                      cacheName: cacheName)
  }
}

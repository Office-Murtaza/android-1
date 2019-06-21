import Foundation
import CoreData

class FetchRequestBuilder<E> where E: NSFetchRequestResult {
  private let request: NSFetchRequest<E>
  
  private var filterPredicate: NSPredicate?
  private var searchPredicate: NSPredicate?
  
  init(request: NSFetchRequest<E> = NSFetchRequest<E>(entityName: String(describing: E.self))) {
    self.request = request
  }
  
  @discardableResult
  func matching(_ predicate: NSPredicate?) -> Self {
    self.filterPredicate = predicate
    return self
  }
  
  @discardableResult
  func sorted(by sortDescriptors: [NSSortDescriptor]?) -> Self {
    request.sortDescriptors = sortDescriptors.map { descriptors in
      request.sortDescriptors.map { $0 + descriptors } ?? descriptors
    }
    return self
  }
  
  @discardableResult
  func sorted<V>(by key: KeyPath<E, V>, ascending: Bool) -> Self {
    let descriptor = NSSortDescriptor(keyPath: key, ascending: ascending)
    return sorted(by: [descriptor])
  }
  
  @discardableResult
  func limit(to limit: Int) -> Self {
    request.fetchLimit = limit
    return self
  }
  
  @discardableResult
  func batch(limit: Int) -> Self {
    request.fetchBatchSize = limit
    return self
  }
  
  @discardableResult
  func prefetch(relationship: String) -> Self {
    return prefetch(relationships: relationship)
  }
  
  @discardableResult
  func prefetch(relationships: String...) -> Self {
    let relationships = request.relationshipKeyPathsForPrefetching.map { $0 + relationships } ?? relationships
    request.relationshipKeyPathsForPrefetching = relationships
    return self
  }
  
  @discardableResult
  func search(by predicate: NSPredicate?) -> Self {
    self.searchPredicate = predicate
    return self
  }
  
  func build() -> NSFetchRequest<E> {
    let predicates = [filterPredicate, searchPredicate].compactMap { $0 }
    request.predicate = NSCompoundPredicate(andPredicateWithSubpredicates: predicates)
    return request
  }
}

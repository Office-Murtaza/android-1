import Foundation
import CoreData
import RxSwift

final class CoreDataObserver<T: NSManagedObject, U: Equatable>: NSObject, NSFetchedResultsControllerDelegate {
  
  typealias UpdatesMatchingBlock = (U, U?) -> Bool
  
  private struct CoreDataObserverChange: Equatable {
    enum ChangeType {
      case insert
      case delete
      case update
    }
    let indexPath: IndexPath
    let element: U?
    let type: ChangeType
  }
  
  private let frcBuilder: FRCBuilder<T>
  private var fetchedResultsController: NSFetchedResultsController<T>!
  private let converter: Converter<T, U>
  
  private var models = [U]()
  private var changes = [CoreDataObserverChange]()
  private let updatesMatchingBlock: UpdatesMatchingBlock?
  
  let itemsChanged = BehaviorSubject<[U]>(value: [])
  
  init(frcBuilder: FRCBuilder<T>,
       converter: Converter<T, U>,
       updatesMatchingBlock: UpdatesMatchingBlock? = nil) {
    self.converter = converter
    self.frcBuilder = frcBuilder
    self.updatesMatchingBlock = updatesMatchingBlock
    super.init()
    fetchObjects()
  }
  
  func fetchObjects() {
    fetchedResultsController = frcBuilder.build()
    
    fetchedResultsController.delegate = self
    try? fetchedResultsController.performFetch()
    
    let records = fetchedResultsController.fetchedObjects ?? []
    models = records.map { try? self.converter.convert(model: $0) }.compactMap {$0}
    notifyObservers()
  }
  
  func notifyObservers() {
    itemsChanged.onNext(models)
  }
  
  private func applyChanges() {
    if changes.isEmpty { return }
    
    changes
      .filter { $0.type == .update }
      .filter { [unowned self] in
        guard let skipUpdateBlock = self.updatesMatchingBlock else { return true }
        let original = models[$0.indexPath.item]
        let updated = $0.element
        return !skipUpdateBlock(original, updated)
      }
      .forEach { models[$0.indexPath.item] = $0.element! }
    
    let indexesToRemove = changes.filter { $0.type == .delete }.map { $0.indexPath.item }
    if indexesToRemove.count == 1 {
      models.remove(at: indexesToRemove[0])
    } else if indexesToRemove.count > 1 {
      models = models.enumerated().filter({ !indexesToRemove.contains($0.0) }).map { $0.1 }
    }
    
    changes
      .filter { $0.type == .insert }
      .sorted(by: {
        $0.indexPath.item < $1.indexPath.item
      })
      .forEach { models.insert($0.element!, at: $0.indexPath.item) }
    
    if changes.isNotEmpty {
      notifyObservers()
    }
  }
  
  // MARK: NSFetchedResultsControllerDelegate
  
  @objc
  func controllerWillChangeContent(_ controller: NSFetchedResultsController<NSFetchRequestResult>) {
    changes.removeAll()
  }
  
  @objc
  func controllerDidChangeContent(_ controller: NSFetchedResultsController<NSFetchRequestResult>) {
    applyChanges()
  }
  
  @objc
  func controller(_ controller: NSFetchedResultsController<NSFetchRequestResult>,
                  didChange anObject: Any,
                  at indexPath: IndexPath?,
                  for type: NSFetchedResultsChangeType,
                  newIndexPath: IndexPath?) {
    guard let object = anObject as? T else { return }
    
    switch type {
    case .insert:
      guard let mapped = try? self.converter.convert(model: object),
        let indexPath = newIndexPath else { return }
      self.changes.append(CoreDataObserverChange(indexPath: indexPath, element: mapped, type: .insert))
    case .update:
      guard let mapped = try? self.converter.convert(model: object),
        let indexPath = indexPath else { return }
      self.changes.append(CoreDataObserverChange(indexPath: indexPath, element: mapped, type: .update))
    case .move:
      guard let mapped = try? self.converter.convert(model: object),
        let newIndexPath = newIndexPath,
        let indexPath = indexPath else { return }
      self.changes.append(CoreDataObserverChange(indexPath: indexPath, element: mapped, type: .delete))
      self.changes.append(CoreDataObserverChange(indexPath: newIndexPath, element: mapped, type: .insert))
    case .delete:
      guard let indexPath = indexPath else { return }
      self.changes.append(CoreDataObserverChange(indexPath: indexPath, element: nil, type: .delete))
    }
  }
}

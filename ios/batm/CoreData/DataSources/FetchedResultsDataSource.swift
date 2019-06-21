import Foundation
import CoreData

protocol FetchedResultsController {
  associatedtype Converter: ConverterType where Converter.FromType: NSFetchRequestResult
  
  func performFetch(with controller: NSFetchedResultsController<Converter.FromType>, converter: Converter) throws
}

class FetchedResultsControllerBox<C: ConverterType>: FetchedResultsController
where C.FromType: NSFetchRequestResult {

  func performFetch(with controller: NSFetchedResultsController<C.FromType>, converter: C) throws {}
}

final class AnyFetchedResultsControllerBox<T: FetchedResultsController>: FetchedResultsControllerBox<T.Converter> {
  let frc: T
  
  init(_ frc: T) {
    self.frc = frc
  }
  
  override func performFetch(with controller: NSFetchedResultsController<T.Converter.FromType>,
                             converter: T.Converter) throws {
   try frc.performFetch(with: controller, converter: converter)
  }
}

class AnyFetchedResultsController<C: ConverterType>: FetchedResultsController where C.FromType: NSFetchRequestResult {
  let frc: FetchedResultsControllerBox<C>
  
  init<T: FetchedResultsController>(controller: T) where T.Converter == C {
    frc = AnyFetchedResultsControllerBox(controller)
  }
  
  func performFetch(with controller: NSFetchedResultsController<C.FromType>, converter: C) throws {
    try frc.performFetch(with: controller, converter: converter)
  }
}

class FetchedResultsDataSource<C: ConverterType>: NSObject,
  SectionedDataSourceType,
  NSFetchedResultsControllerDelegate,
  FetchedResultsController
where C.FromType: NSFetchRequestResult {

  typealias Section = SectionInfo<C.ToType>
  typealias Change = SectionedDataSourceChange<Section>
  
  private var observers = SectionedDataSourceObservable<AnySectionedDataSourceObserver<Section>>()
  private var _sections: [Section?] = []
  
  private let executor: Executor
  
  private var controller: NSFetchedResultsController<C.FromType>! {
    willSet {
      controller.flatMap { $0.delegate = nil }
    }
  }
  private var converter: C!
  private var currentChanges: [Change] = []
  
  init(executor: @escaping Executor = asyncExecutor(queue: DispatchQueue.global(qos: .userInteractive))) {
    self.executor = executor
  }
  
  // TO_DO stop fetching
  func performFetch(with controller: NSFetchedResultsController<C.FromType>, converter: C) throws {
    self.controller = controller
    self.converter = converter
    try self.startFetching()
  }
  
  func startFetching() throws {
    executor { [weak self] in
      do {
        try self?.controller.performFetch()
        try self?.loadSections()
        guard let controller = self?.controller else { return }
        controller.delegate = self
      } catch {
        print("FetchedResultsDataSourceError:", error)
      }
    }
  }
  
  private func loadSections() throws {
    sections = try controller.sections?.map { try convert($0) } ?? []
  }
  
  private func convert(_ section: NSFetchedResultsSectionInfo) throws -> Section {
    let elements = try section.objects?
      .map { try convert($0) } ?? []
    return Section(name: section.name, elements: elements)
  }
  
  private func convert(_ element: Any) throws -> C.ToType {
    //  swiftlint:disable force_cast
    return try converter.convert(model: element as! C.FromType)
    //  swiftlint:enable force_cast
  }

  // MARK: SectionedDataSourceType

  var sections: [Section] {
    set {
      _sections = newValue
      observers.didReload(self)
    }
    get {
      return compactSections
    }
  }
  
  var elements: [Section.Element] {
    return sections.flatMap { $0 }
  }
  
  func element(at indexPath: IndexPath) -> Section.Element {
    return sections[indexPath.section][indexPath.row]
  }
  
  func add<O: SectionedDataSourceObserverType>(observer: O) where O.Section == Section {
    observers.add(AnySectionedDataSourceObserver(observer))
  }
  
  func remove<O: SectionedDataSourceObserverType>(observer: O) where O.Section == Section {
    observers.remove(AnySectionedDataSourceObserver(observer))
  }
  
  private func applyChanges(changes: [Change]) {
    changes
      .sorted()
      .forEach {
        switch $0 {
        case let .section(section, change):
          apply(change, for: section)
        case let .element(element, change):
          apply(change, for: element)
        }
    }
  }
  
  private func apply(_ change: Change.Section, for section: Section) {
    switch change {
    case let .insert(index):
      insert(section, at: index)
    case let .delete(index):
      deleteSection(at: index)
    }
  }
  
  private func apply(_ change: Change.Element, for element: C.ToType?) {
    switch change {
    case let .insert(indexPath):
      guard let unwrapped = element else { fatalError("Element can't be nil in insert operation") }
      insert(unwrapped, at: indexPath)
    case let .delete(indexPath):
      delete(at: indexPath)
    case let .update(indexPath):
      guard let unwrapped = element else { fatalError("Element can't be nil in update operation") }
      update(unwrapped, at: indexPath)
    }
  }
  
  private func insert(_ section: Section, at index: Int) {
    compact()
    fillSection(to: index)
    _sections.insert(Section(name: section.name), at: index)
  }
  
  @discardableResult
  private func deleteSection(at index: Int) -> Section {
    var section = _sections.remove(at: index)!
    _sections.insert(nil, at: index)
    section.compact()
    return section
  }
  
  private func insert(_ element: Section.Element, at indexPath: IndexPath) {
    compact()
    var section = _sections[indexPath.section]!
    section.insert(element, at: indexPath.row)
    _sections[indexPath.section] = section
  }
  
  private func update(_ element: Section.Element, at indexPath: IndexPath) {
    var section = _sections[indexPath.section]!
    section[indexPath.row] = element
    _sections[indexPath.section] = section
  }
  
  @discardableResult
  private func delete(at indexPath: IndexPath) -> Section.Element {
    var section = _sections[indexPath.section]!
    let element = section.remove(at: indexPath.row)
    _sections[indexPath.section] = section
    return element
  }
  
  private var compactSections: [Section] {
    return _sections.compactMap { $0 }
  }
  
  private func compact() {
    _sections = compactSections
  }
  
  private func compactAll() {
    _sections = compactSections
      .map {
        var section = $0
        section.compact()
        return section
    }
  }
  
  private func fillSection(to index: Int) {
    let stubs = Array(repeating: Section?.none, count: Swift.max(index - _sections.endIndex, 0))
    _sections.append(contentsOf: stubs)
  }
  
  // MARK: NSFetchedResultsControllerDelegate
  
  @objc
  func controllerWillChangeContent(_ controller: NSFetchedResultsController<NSFetchRequestResult>) {
      currentChanges.removeAll()
  }
  
  private var lastUpdate = CFAbsoluteTimeGetCurrent()
  private var changesAlreadyApplied = false
  
  @objc
  func controllerDidChangeContent(_ controller: NSFetchedResultsController<NSFetchRequestResult>) {
    let changes = self.currentChanges.sorted()
    self.observers.didChangeContent(self, changes: changes, applyBlock: { [weak self] in
      self?.applyChanges(changes: changes)
      self?.compactAll()
    })
  }
  
  @objc
  func controller(_ controller: NSFetchedResultsController<NSFetchRequestResult>,
                  didChange sectionInfo: NSFetchedResultsSectionInfo,
                  atSectionIndex sectionIndex: Int,
                  for type: NSFetchedResultsChangeType) {
    do {
      let section = try convert(sectionInfo)
      switch type {
      case .insert:
        currentChanges.append(.section(section, .insert(at: sectionIndex)))
      case .delete:
        currentChanges.append(.section(section, .delete(from: sectionIndex)))
      default:
        break
      }
    } catch {
      fatalError(String(describing: error))
    }
  }
  
  @objc
  func controller(_ controller: NSFetchedResultsController<NSFetchRequestResult>,
                  didChange anObject: Any,
                  at indexPath: IndexPath?,
                  for type: NSFetchedResultsChangeType,
                  newIndexPath: IndexPath?) {
    do {
      switch type {
      case .insert:
        if let indexPath = newIndexPath {
          let element = try convert(anObject)
          currentChanges.append(.element(element, .insert(at: indexPath)))
        }
      case .update:
        if let indexPath = indexPath {
          let element = try convert(anObject)
          currentChanges.append(.element(element, .update(at: indexPath)))
        }
      case .move:
        let element = try convert(anObject)
        guard let newIndexPath = newIndexPath, let indexPath = indexPath else { break }
        currentChanges.append(.element(element, .delete(from: indexPath)))
        currentChanges.append(.element(element, .insert(at: newIndexPath)))
      case .delete:
        if let indexPath = indexPath {
          currentChanges.append(.element(nil, .delete(from: indexPath)))
        }
      }
    } catch {
      fatalError(String(describing: error))
    }
  }
}

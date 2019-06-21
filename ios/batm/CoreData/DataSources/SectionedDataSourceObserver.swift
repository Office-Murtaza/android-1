import Foundation

protocol SectionedDataSourceObserverType: class {
  associatedtype Section: SectionType
  
  typealias Change = SectionedDataSourceChange<Section>
  
  var executor: Executor { get }
  
  func dataSourceDidChangeContent<D: SectionedDataSourceType>(_ dataSource: D,
                                                              changes: [Change],
                                                              applyBlock: @escaping () -> Void)
    where D.Section == Section
  
  func dataSourceDidReload<D: SectionedDataSourceType>(_ dataSource: D) where D.Section == Section
}

extension SectionedDataSourceObserverType {
  var executor: Executor {
    return asyncExecutor(queue: DispatchQueue.main)
  }
}

class AnySectionedDataSourceObserverBase<S: SectionType>: SectionedDataSourceObserverType {
  
  typealias Change = SectionedDataSourceChange<S>
  
  func dataSourceDidChangeContent<D: SectionedDataSourceType>(_ dataSource: D,
                                                              changes: [Change],
                                                              applyBlock: @escaping () -> Void) where D.Section == S {}
  
  func dataSourceDidReload<D: SectionedDataSourceType>(_ dataSource: D) where D.Section == S {}
}

final class AnySectionedDataSourceObserverBaseBox<O>: AnySectionedDataSourceObserverBase<O.Section>
where O: SectionedDataSourceObserverType {
  
  typealias Change = SectionedDataSourceChange<O.Section>
  
  weak var observer: O? 
  
  init(observer: O) {
    self.observer = observer
  }
  
  override func dataSourceDidChangeContent<D: SectionedDataSourceType>(_ dataSource: D,
                                                                       changes: [Change],
                                                                       applyBlock: @escaping () -> Void)
    where D.Section == O.Section {
      observer?.dataSourceDidChangeContent(dataSource, changes: changes, applyBlock: applyBlock)
  }
  
  override func dataSourceDidReload<D: SectionedDataSourceType>(_ dataSource: D)
    where D.Section == O.Section {
      observer?.dataSourceDidReload(dataSource)
  }
}

final class AnySectionedDataSourceObserver<Section: SectionType>: SectionedDataSourceObserverType {
  
  typealias Change = SectionedDataSourceChange<Section>
  
  let hashValue: Int
  
  private let box: AnySectionedDataSourceObserverBase<Section>
  let executor: Executor
  weak var observer: AnyObject?
  
  init<Concrete: SectionedDataSourceObserverType>(_ concrete: Concrete)
    where Concrete.Section == Section {
      hashValue = ObjectIdentifier(concrete).hashValue
      let baseBox = AnySectionedDataSourceObserverBaseBox(observer: concrete)
      observer = baseBox.observer
      executor = concrete.executor
      box = baseBox
  }
  
  func dataSourceDidChangeContent<D: SectionedDataSourceType>(_ dataSource: D,
                                                              changes: [Change],
                                                              applyBlock: @escaping () -> Void)
    where D.Section == Section {
      box.dataSourceDidChangeContent(dataSource, changes: changes, applyBlock: applyBlock)
  }
  
  func dataSourceDidReload<D: SectionedDataSourceType>(_ dataSource: D)
    where D.Section == Section {
      box.dataSourceDidReload(dataSource)
  }
}

extension AnySectionedDataSourceObserver: Hashable {
  static func == (lhs: AnySectionedDataSourceObserver<Section>,
                  rhs: AnySectionedDataSourceObserver<Section>) -> Bool {
    switch (lhs.observer, rhs.observer) {
    case let (.some(lhso), .some(rhs)):
      return lhso === rhs
    default:
      return false
    }
  }
}

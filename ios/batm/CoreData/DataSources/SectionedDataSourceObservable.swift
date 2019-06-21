import Foundation

enum SectionedDataSourceChange<S: SectionType> {
  enum Element: Equatable {
    case insert(at: IndexPath)
    case delete(from: IndexPath)
    case update(at: IndexPath)
  }
  
  enum Section: Equatable {
    case insert(at: Int)
    case delete(from: Int)
  }
  
  case element(S.Element?, Element)
  case section(S, Section)
}

protocol SectionedDataSourceObservableType {
  associatedtype Section: SectionType
  
  func add<O: SectionedDataSourceObserverType>(observer: O) where O.Section == Section
  func remove<O: SectionedDataSourceObserverType>(observer: O) where O.Section == Section
}

class SectionedDataSourceObservable<Observer: SectionedDataSourceObserverType & Hashable> {
  typealias Change = SectionedDataSourceChange<Observer.Section>
  private var observers = Set<Observer>()
  private let lock = RecursiveLock()
  
  func add(_ observer: Observer) {
    lock.calculateLocked {
      _ = observers.insert(observer)
    }
  }
  
  func remove(_ observer: Observer) {
    lock.calculateLocked {
      _ = observers.remove(observer)
    }
  }
  
  func didReload<D: SectionedDataSourceType>(_ dataSource: D)
    where D.Section == Observer.Section {
      lock.calculateLocked {
        observers.forEach { observer in
          observer.executor { observer.dataSourceDidReload(dataSource) }
        }
      }
  }
  
  func didChangeContent<D: SectionedDataSourceType>(_ dataSource: D,
                                                    changes: [Change],
                                                    applyBlock: @escaping () -> Void)
    where D.Section == Observer.Section {
      lock.calculateLocked {
        observers.forEach { observer in
          observer.executor {
            observer.dataSourceDidChangeContent(dataSource, changes: changes, applyBlock: applyBlock)
          }
        }
      }
  }
}

extension SectionedDataSourceChange: CustomStringConvertible {
  var description: String {
    switch self {
    case let .element(element, change):
      return "element \(String(describing: element)): \(change)"
    case let .section(section, change):
      return "section \(section): \(change)"
    }
  }
}

extension SectionedDataSourceChange: Comparable {
  typealias Change = SectionedDataSourceChange
  
  static func < (lhs: Change, rhs: Change) -> Bool {
    switch (lhs, rhs) {
    case let (.element(_, lhs), .element(_, rhs)):
      return lhs < rhs
    case let (.section(_, lhs), .section(_, rhs)):
      return lhs < rhs
    case let (.element(_, element), .section(_, section)):
      switch (element, section) {
      case (.update, _),
           (.delete, _):
        return true
      default:
        return false
      }
    case let (.section(_, section), .element(_, element)):
      switch (element, section) {
      case (.update, _),
           (.delete, _):
        return false
      default:
        return true
      }
    }
  }
  
  static func == (lhs: Change, rhs: Change) -> Bool {
    switch (lhs, rhs) {
    case let (.element(_, lhs), .element(_, rhs)):
      return lhs == rhs
    case let (.section(_, lhs), .section(_, rhs)):
      return lhs == rhs
    default:
      return false
    }
  }
}

extension SectionedDataSourceChange.Element: Comparable {
  typealias Element = SectionedDataSourceChange.Element
  
  static func < (lhs: Element, rhs: Element) -> Bool {
    switch (lhs, rhs) {
    case (.update, .delete),
         (.update, .insert):
      return true
    case (.delete, .insert):
      return true
    case let (.update(lhs), .update(rhs)):
      return lhs < rhs
    case let (.delete(lhs), .delete(rhs)):
      return lhs < rhs
    case let (.insert(lhs), .insert(rhs)):
      return lhs < rhs
    default:
      return false
    }
  }
}

extension SectionedDataSourceChange.Section: Comparable {
  typealias Section = SectionedDataSourceChange.Section
  
  static func < (lhs: Section, rhs: Section) -> Bool {
    switch (lhs, rhs) {
    case (.delete, .insert):
      return true
    case let (.delete(lhs), .delete(rhs)):
      return lhs < rhs
    case let (.insert(lhs), .insert(rhs)):
      return lhs < rhs
    default:
      return false
    }
  }
}

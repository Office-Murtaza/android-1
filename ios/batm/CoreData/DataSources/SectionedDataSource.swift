import Foundation

protocol SectionType: MutableCollection & RangeReplaceableCollection where Index == Int {
  var name: String { get }
}

extension Array: SectionType {
  var name: String {
    return ""
  }
}

protocol SectionedDataSourceType: SectionedDataSourceObservableType {
  var sections: [Section] { get }
  var elements: [Section.Element] { get }

  func element(at indexPath: IndexPath) -> Section.Element

}

extension SectionedDataSourceType where Section.Element: Equatable {
  func indexPath(for element: Section.Element) -> IndexPath? {
    return sections
      .enumerated()
      .map { (row: $0.element.index(of: element), section: $0.offset) }
      .first { $0.row != nil }
      .map { IndexPath(row: $0.row!, section: $0.section) }
  }
}

extension SectionedDataSourceType where Section.Element: AnyObject {
  func indexPath(for element: Section.Element) -> IndexPath? {
    return sections
      .enumerated()
      .map { value -> (row: Int?, section: Int) in
        let index = value.element.index { $0 === element }
        return (row: index, section: value.offset) }
      .first { $0.row != nil }
      .map { IndexPath(row: $0.row!, section: $0.section) }
  }
}

class SectionedDataSource<S: SectionType>: SectionedDataSourceType {
  
  typealias Section = S
  
  private var observers = SectionedDataSourceObservable<AnySectionedDataSourceObserver<S>>()
  private var _sections = [S([])]
  
  var sections: [Section] {
    set {
      _sections = newValue
      observers.didReload(self)
    }
    get {
      return _sections
    }
  }
  
  var elements: [Section.Element] {
    return sections.flatMap { $0 }
  }

  func element(at indexPath: IndexPath) -> Section.Element {
    return sections[indexPath.section][indexPath.row]
  }
  
  func insert(_ section: S, at index: Int) {
    _sections.insert(section, at: index)
  }
  
  @discardableResult
  func deleteSection(at index: Int) -> S {
    let section = _sections.remove(at: index)
    return section
  }
  
  func insert(_ element: S.Element, at indexPath: IndexPath) {
    var section = _sections[indexPath.section]
    section.insert(element, at: indexPath.row)
    _sections[indexPath.section] = section
  }
  
  func update(_ element: S.Element, at indexPath: IndexPath) {
    var section = _sections[indexPath.section]
    section[indexPath.row] = element
    _sections[indexPath.section] = section
  }
  
  @discardableResult
  func delete(at indexPath: IndexPath) -> S.Element? {
    var section = _sections[indexPath.section]
    let element = section.remove(at: indexPath.row)
    _sections[indexPath.section] = section
    return element
  }
  
  func add<O: SectionedDataSourceObserverType>(observer: O) where O.Section == Section {
    observers.add(AnySectionedDataSourceObserver(observer))
  }
  
  func remove<O: SectionedDataSourceObserverType>(observer: O) where O.Section == Section {
    observers.remove(AnySectionedDataSourceObserver(observer))
  }
}

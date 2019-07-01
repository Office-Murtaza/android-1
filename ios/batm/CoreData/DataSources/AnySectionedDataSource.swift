import Foundation

class AnySectionedDataSourceBase<S: SectionType>: SectionedDataSourceType {
  
  typealias Section = S
  
  func element(at indexPath: IndexPath) -> S.Element {
    fatalError()
  }
  
  var sections: [S] {
    fatalError()
  }
  
  var elements: [S.Element] {
    fatalError()
  }
  
  func add<O: SectionedDataSourceObserverType>(observer: O) where O.Section == S {
    fatalError()
  }
  
  func remove<O: SectionedDataSourceObserverType>(observer: O) where O.Section == S {
    fatalError()
  }
}

final class AnySectionedDataSourceBaseBox<D: SectionedDataSourceType>: AnySectionedDataSourceBase<D.Section> {
  
  private let dataSource: D
  
  init(dataSource: D) {
    self.dataSource = dataSource
  }
  
  override func element(at indexPath: IndexPath) -> D.Section.Element {
    return dataSource.element(at: indexPath)
  }
  
  override var sections: [D.Section] {
    return dataSource.sections
  }
  
  override var elements: [D.Section.Element] {
    return dataSource.elements
  }
  
  override func add<O: SectionedDataSourceObserverType>(observer: O)
    where O.Section == D.Section {
      dataSource.add(observer: observer)
  }
  
  override func remove<O: SectionedDataSourceObserverType>(observer: O)
    where O.Section == D.Section {
      dataSource.remove(observer: observer)
  }
}

class AnySectionedDataSource<Section: SectionType>: SectionedDataSourceType {

  private let box: AnySectionedDataSourceBase<Section>
  
  init<D: SectionedDataSourceType>(_ dataSource: D)
    where D.Section == Section {
      box = AnySectionedDataSourceBaseBox(dataSource: dataSource)
  }
  
  func element(at indexPath: IndexPath) -> Section.Element {
    return box.element(at: indexPath)
  }
  
  var sections: [Section] {
    return box.sections
  }
  
  var elements: [Section.Element] {
    return box.elements
  }
  
  func add<O: SectionedDataSourceObserverType>(observer: O) where O.Section == Section {
    box.add(observer: observer)
  }
  
  func remove<O: SectionedDataSourceObserverType>(observer: O) where O.Section == Section {
    box.remove(observer: observer)
  }
}

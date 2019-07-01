import Foundation
import UIKit

protocol Refreshable: class {
  func reload()
  func performUpdates(_ updateClosure: @escaping () -> Void,
                      completion: ((Bool) -> Void)?)
  
  func insert(rows: [IndexPath])
  func reload(rows: [IndexPath])
  func delete(rows: [IndexPath])
  
  func insert(sections: IndexSet)
  func delete(sections: IndexSet)
}

final class RefreshAdapter<Section: SectionType>: SectionedDataSourceObserverType {
  
  weak var view: Refreshable?
  
  private func didChange(_ element: Section.Element?,
                         for type: SectionedDataSourceChange<Section>.Element) {
    switch type {
    case let .insert(indexPath):
      view?.insert(rows: [indexPath])
    case let .delete(indexPath):
      view?.delete(rows: [indexPath])
    case let .update(indexPath):
      view?.reload(rows: [indexPath])
    }
  }
  
  private func didChange(_ sectionInfo: Section,
                         for type: SectionedDataSourceChange<Section>.Section) {
    switch type {
    case let .insert(indexPath):
      view?.insert(sections: [indexPath])
    case let .delete(indexPath):
      view?.delete(sections: [indexPath])
    }
  }
  
  func dataSourceDidChangeContent<D: SectionedDataSourceType>(_ dataSource: D,
                                                              changes: [SectionedDataSourceChange<Section>],
                                                              applyBlock: @escaping () -> Void)
    where D.Section == Section {

      view?.performUpdates({
        applyBlock()

        changes.forEach {
          switch $0 {
          case let .section(section, type):
            self.didChange(section, for: type)
          case let .element(element, type):
            self.didChange(element, for: type)
          }
        }
      }, completion: nil)
  }
  
  func dataSourceDidReload<D: SectionedDataSourceType>(_ dataSource: D)
    where D.Section == Section {
      view?.reload()
  }
}

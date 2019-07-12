import Foundation

final class ReloadAdapter<Section: SectionType>: SectionedDataSourceObserverType {
  
  weak var view: Refreshable?
  
  var executor: Executor {
    return asyncExecutor(queue: DispatchQueue.main)
  }
  
  func dataSourceDidChangeContent<D: SectionedDataSourceType>(_ dataSource: D,
                                                              changes: [Change],
                                                              applyBlock: () -> Void)
    where D.Section == Section {
      applyBlock()
      view?.reload()
  }
  
  func dataSourceDidReload<D: SectionedDataSourceType>(_ dataSource: D) where D.Section == Section {
    view?.reload()
  }
}

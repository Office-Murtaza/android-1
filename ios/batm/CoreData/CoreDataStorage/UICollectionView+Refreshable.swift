import Foundation
import UIKit

extension UICollectionView: Refreshable {
  func insert(sections: IndexSet) {
    insertSections(sections)
  }
  
  func delete(sections: IndexSet) {
    deleteSections(sections)
  }
  
  func insert(rows: [IndexPath]) {
    insertItems(at: rows)
  }
  
  func reload(rows: [IndexPath]) {
    reloadItems(at: rows)
  }
  
  func delete(rows: [IndexPath]) {
    deleteItems(at: rows)
  }
  
  func reload() {
    reloadData()
  }
  
  func performUpdates(_ updateClosure: @escaping () -> Void, completion: ((Bool) -> Void)?) {
    performBatchUpdates(updateClosure, completion: completion)
  }
}

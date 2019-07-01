import Foundation
import UIKit

extension UITableView: Refreshable {
  
  func insert(sections: IndexSet) {
    insertSections(sections, with: .automatic)
  }
  
  func delete(sections: IndexSet) {
    deleteSections(sections, with: .automatic)
  }
  
  func insert(rows: [IndexPath]) {
    insertRows(at: rows, with: .automatic)
  }
  
  func reload(rows: [IndexPath]) {
    reloadRows(at: rows, with: .none)
  }
  
  func delete(rows: [IndexPath]) {
    deleteRows(at: rows, with: .automatic)
  }
  
  func reload() {
    reloadData()
  }
  
  func performUpdates(_ updateClosure: @escaping () -> Void, completion: ((Bool) -> Void)?) {
    performBatchUpdates(updateClosure, completion: completion)
  }
}

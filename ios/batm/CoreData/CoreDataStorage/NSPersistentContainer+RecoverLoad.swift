import Foundation
import CoreData

extension NSPersistentContainer {
  typealias Load = (Bool) -> Void
  typealias RetryLoad = (Bool, @escaping Load) -> Void
  
  private func retryLoad(load: @escaping RetryLoad ) -> Load {
    return { retry in load(retry, self.retryLoad(load: load)) }
  }
  
  func loadPersistentStores() {
    let load = retryLoad { retry, job in
      if retry {
        self.loadPersistentStores { descriptor, error in
          if error != nil {
            do {
              try descriptor.url.map { try FileManager.default.removeItem(at: $0) }
              job(false)
            } catch {
              fatalError(String(describing: error))
            }
          }
        }
      } else {
        self.loadPersistentStores { _, error in
          guard let storeError = error else { return }
          fatalError(String(describing: storeError))
        }
      }
    }
    load(true)
  }
}

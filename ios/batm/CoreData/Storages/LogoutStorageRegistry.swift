import Foundation
import RxSwift

protocol ClearOnLogoutStorage: AnyObject {
  func clear() -> Completable
}

protocol LogoutStorageRegistry {
  func add(storage: ClearOnLogoutStorage)
  func clear() -> Completable
}

class LogoutStorageRegistryImpl: LogoutStorageRegistry {
  private let lock = RecursiveLock()
  private var storages = [ClearOnLogoutStorage]()
  
  func add(storage: ClearOnLogoutStorage) {
    lock.calculateLocked {
      self.storages.append(storage)
    }
  }
  
  func clear() -> Completable {
    return lock.calculateLocked {
      guard self.storages.count > 0 else { return .empty() }
      return Completable.merge(self.storages.map { $0.clear() })
    }
  }
}

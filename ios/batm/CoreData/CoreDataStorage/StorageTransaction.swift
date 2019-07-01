import Foundation
import CoreData
import RxSwift

protocol StorageTransactionExecutor {
  func perform<T>(transaction: @escaping (NSManagedObjectContext) throws -> T) -> Single<T>
}

class StorageTransactionExecutorImpl: StorageTransactionExecutor {
  private let context: NSManagedObjectContext

  init(context: NSManagedObjectContext) {
    self.context = context
  }
  
  func perform<T>(transaction: @escaping (NSManagedObjectContext) throws -> T) -> Single<T> {
    return Single.create { [context] single -> Disposable in
      context.perform { [context] in
        do {
          let result = try transaction(context)
          single(.success(result))
        } catch {
          single(.error(error))
        }
      }
      return Disposables.create()
    }
  }
}

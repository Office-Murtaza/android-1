import Foundation
import CoreData
import RxSwift

protocol StorageUtils {
  init(context: NSManagedObjectContext)
}

enum CoreDataStorageErrors: Error {
  case notFound
}

class CoreDataStorage<U: StorageUtils> {
  
  private let executor: StorageTransactionExecutor

  init(transactionExecutor: StorageTransactionExecutor) {
    executor = transactionExecutor
  }
  
    func save(transaction: @escaping (U) throws -> Void, completion: (() -> Void)? = nil) -> Completable {
    return executor.perform { context in
      let utils = U(context: context)
      try transaction(utils)
      try context.save()
      completion?()
      }.toCompletable()
  }
  
  func save(transaction: @escaping (U) throws -> Completable, completion: (() -> Void)? = nil) -> Completable {
    return executor.perform { context -> Completable in
      let utils = U(context: context)
      let result = try transaction(utils)
      try context.save()
      completion?()
      return result
      }.toCompletable()
  }
  
  func save<T>(transaction: @escaping (U) throws -> T, completion: (() -> Void)? = nil) -> Single<T> {
    return executor.perform { context in
      let utils = U(context: context)
      let result = try transaction(utils)
      try context.save()
      completion?()
      return result
      }
  }
  
  func fetch<T>(transaction: @escaping (U) throws -> T) -> Single<T> {
    return executor.perform { context in
      let utils = U(context: context)
      return try transaction(utils)
    }
  }
}

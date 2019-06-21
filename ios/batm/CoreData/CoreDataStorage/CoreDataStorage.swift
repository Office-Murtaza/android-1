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
  
  func save(transaction: @escaping (U) throws -> Void) -> Completable {
    return executor.perform { context in
      let utils = U(context: context)
      try transaction(utils)
      try context.save()
      }.toCompletable()
  }
  
  func save(transaction: @escaping (U) throws -> Completable) -> Completable {
    return executor.perform { context -> Completable in
      let utils = U(context: context)
      let result = try transaction(utils)
      try context.save()
      return result
      }.toCompletable()
  }
  
  func save<T>(transaction: @escaping (U) throws -> T) -> Single<T> {
    return executor.perform { context in
      let utils = U(context: context)
      let result = try transaction(utils)
      try context.save()
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

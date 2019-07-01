import Foundation
import CoreData
import RxSwift

protocol AccountStorage: ClearOnLogoutStorage {
  func save(account: Account) -> Completable
  func get() -> Single<Account>
  func delete() -> Completable
}

class AccountStorageImpl: CoreDataStorage<AccountStorageUtils>, AccountStorage {
  
  func save(account: Account) -> Completable {
    return save {
      try $0.save(account: account)
    }
  }
  
  func get() -> Single<Account> {
    return fetch {
      return try $0.get()
    }
  }
  
  func clear() -> Completable {
    return delete()
  }
  
  func delete() -> Completable {
    return save {
      try $0.delete()
    }
  }
  
}

class AccountStorageUtils: StorageUtils {
  
  let context: NSManagedObjectContext
  let converter = AccountConverter()
  
  required init(context: NSManagedObjectContext) {
    self.context = context
  }
  
  func save(account: Account) throws {
    try AccountRecord.findOrCreate(in: context, account: account)
  }
  
  func get() throws -> Account {
    guard let accountRecord = try AccountRecord.fetchFirst(in: context) else {
      throw StorageError.notFound
    }
    
    return try converter.convert(model: accountRecord)
  }
  
  func delete() throws {
    try AccountRecord.fetchAndDelete(in: context)
  }
  
}

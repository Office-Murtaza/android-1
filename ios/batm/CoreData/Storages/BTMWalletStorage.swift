import Foundation
import CoreData
import RxSwift

protocol BTMWalletStorage: ClearOnLogoutStorage {
  func save(wallet: BTMWallet) -> Completable
  func get() -> Single<BTMWallet>
  func delete() -> Completable
}

class BTMWalletStorageImpl: CoreDataStorage<BTMWalletStorageUtils>, BTMWalletStorage {
  
  func save(wallet: BTMWallet) -> Completable {
    return save {
      try $0.save(wallet: wallet)
    }
  }
  
  func get() -> Single<BTMWallet> {
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

class BTMWalletStorageUtils: StorageUtils {
  
  let context: NSManagedObjectContext
  let converter = BTMWalletConverter()
  
  required init(context: NSManagedObjectContext) {
    self.context = context
  }
  
  func save(wallet: BTMWallet) throws {
    try delete()
    try BTMWalletRecord.findOrCreate(in: context, wallet: wallet)
  }
  
  func get() throws -> BTMWallet {
    guard let walletRecord = try BTMWalletRecord.fetchFirst(in: context) else {
      throw StorageError.notFound
    }
    
    return try converter.convert(model: walletRecord)
  }
  
  func delete() throws {
    try BTMWalletRecord.fetchAndDelete(in: context)
  }
  
}

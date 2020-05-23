import Foundation
import CoreData
import RxSwift
import TrustWalletCore

protocol LocationUpdateDateStorage: ClearOnLogoutStorage {
  func save(updateDate: Date) -> Completable
  func get() -> Single<Date>
  func delete() -> Completable
}

class LocationUpdateDateStorageImpl: CoreDataStorage<LocationUpdateDateStorageUtils>, LocationUpdateDateStorage {
  
  func save(updateDate: Date) -> Completable {
    return save {
      try $0.save(updateDate: updateDate)
    }
  }
  
  func get() -> Single<Date> {
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

class LocationUpdateDateStorageUtils: StorageUtils {
  
  let context: NSManagedObjectContext
  let converter = LocationUpdateDateConverter()
  
  required init(context: NSManagedObjectContext) {
    self.context = context
  }
  
  func save(updateDate: Date) throws {
    try LocationUpdateDateRecord.findOrCreate(in: context, updateDate: updateDate)
  }
  
  func get() throws -> Date {
    guard let locationUpdateDateRecord = try LocationUpdateDateRecord.fetchFirst(in: context) else {
      throw StorageError.notFound
    }
    
    return try converter.convert(model: locationUpdateDateRecord)
  }
  
  func delete() throws {
    try LocationUpdateDateRecord.fetchAndDelete(in: context)
  }
  
}

import Foundation
import CoreData
import RxSwift

protocol PinCodeStorage: ClearOnLogoutStorage {
  func save(pinCode: String) -> Completable
  func get() -> Single<String>
  func delete() -> Completable
}

enum PinCodeStorageError: Error {
  case notFound
}

class PinCodeStorageImpl: CoreDataStorage<PinCodeStorageUtils>, PinCodeStorage {
  
  func save(pinCode: String) -> Completable {
    return save {
      try $0.save(pinCode: pinCode)
    }
  }
  
  func get() -> Single<String> {
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

class PinCodeStorageUtils: StorageUtils {
  
  let context: NSManagedObjectContext
  let converter = PinCodeConverter()
  
  required init(context: NSManagedObjectContext) {
    self.context = context
  }
  
  func save(pinCode: String) throws {
    try delete()
    try PinCodeRecord.findOrCreate(in: context, pinCode: pinCode)
  }
  
  func get() throws -> String {
    guard let pinCodeRecord = try PinCodeRecord.fetchFirst(in: context) else {
      throw PinCodeStorageError.notFound
    }
    
    return try converter.convert(model: pinCodeRecord)
  }
  
  func delete() throws {
    try PinCodeRecord.fetchAndDelete(in: context)
  }
  
}

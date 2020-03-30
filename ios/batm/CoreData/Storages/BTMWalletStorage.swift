import Foundation
import CoreData
import RxSwift
import TrustWalletCore

protocol BTMWalletStorage: ClearOnLogoutStorage {
  func save(wallet: BTMWallet) -> Completable
  func get() -> Single<BTMWallet>
  func changeVisibility(of coin: BTMCoin) -> Completable
  func changeIndex(of type: CoinType, with index: Int) -> Completable
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
  
  func changeVisibility(of coin: BTMCoin) -> Completable {
    return save {
      try $0.changeVisibility(of: coin)
    }
  }
  
  func changeIndex(of type: CoinType, with index: Int) -> Completable {
    return save {
      try $0.changeIndex(of: type, with: index)
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
  
  func changeVisibility(of coin: BTMCoin) throws {
    guard let walletRecord = try BTMWalletRecord.fetchFirst(in: context) else {
      throw StorageError.notFound
    }
    
    let coinRecord = walletRecord.coins.first { $0.type == coin.type.rawValue }
    guard let unwrappedCoinRecord = coinRecord else {
      throw StorageError.notFound
    }
    
    unwrappedCoinRecord.visible.toggle()
  }
  
  func changeIndex(of type: CoinType, with index: Int) throws {
    guard let walletRecord = try BTMWalletRecord.fetchFirst(in: context) else {
      throw StorageError.notFound
    }
    
    let coinRecord = walletRecord.coins.first { $0.type == type.rawValue }
    guard let unwrappedCoinRecord = coinRecord else {
      throw StorageError.notFound
    }
    
    unwrappedCoinRecord.index = Int32(index)
  }
  
  func delete() throws {
    try BTMWalletRecord.fetchAndDelete(in: context)
  }
  
}

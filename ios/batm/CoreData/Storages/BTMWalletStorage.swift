import Foundation
import CoreData
import RxSwift
import TrustWalletCore

protocol BTMWalletStorage: ClearOnLogoutStorage {
  func save(wallet: BTMWallet) -> Completable
  func get() -> Single<BTMWallet>
  func changeVisibility(of coin: BTMCoin) -> Completable
  func changeIndex(of type: CoinType, with index: Int) -> Completable
  func changeFee(of type: CoinType, with fee: Double) -> Completable
  func changeGas(of type: CoinType, price gasPrice: Int, limit gasLimit: Int) -> Completable
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
  
  func changeFee(of type: CoinType, with fee: Double) -> Completable {
    return save {
      try $0.changeFee(of: type, with: fee)
    }
  }
  
  func changeGas(of type: CoinType, price gasPrice: Int, limit gasLimit: Int) -> Completable {
    return save {
      try $0.changeGas(of: type, price: gasPrice, limit: gasLimit)
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
  
  func changeFee(of type: CoinType, with fee: Double) throws {
    guard let walletRecord = try BTMWalletRecord.fetchFirst(in: context) else {
      throw StorageError.notFound
    }
    
    let coinRecord = walletRecord.coins.first { $0.type == type.rawValue }
    guard let unwrappedCoinRecord = coinRecord else {
      throw StorageError.notFound
    }
    
    unwrappedCoinRecord.fee = fee
  }
  
  func changeGas(of type: CoinType, price gasPrice: Int, limit gasLimit: Int) throws {
    guard let walletRecord = try BTMWalletRecord.fetchFirst(in: context) else {
      throw StorageError.notFound
    }
    
    let coinRecord = walletRecord.coins.first { $0.type == type.rawValue }
    guard let unwrappedCoinRecord = coinRecord else {
      throw StorageError.notFound
    }
    
    unwrappedCoinRecord.gasPrice = Int64(gasPrice)
    unwrappedCoinRecord.gasLimit = Int64(gasLimit)
  }
  
  func delete() throws {
    try BTMWalletRecord.fetchAndDelete(in: context)
  }
  
}

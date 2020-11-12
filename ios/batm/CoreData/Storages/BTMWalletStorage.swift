import Foundation
import CoreData
import RxSwift
import RxCocoa
import TrustWalletCore

protocol BTMWalletStorage: ClearOnLogoutStorage {
  func save(wallet: BTMWallet) -> Completable
  func get() -> Single<BTMWallet>
  func changeVisibility(of coin: BTMCoin) -> Completable
  func changeIndex(of type: CustomCoinType, with index: Int) -> Completable
  func delete() -> Completable
  var coinChanged: Observable<Void> { get }
}

enum BTMWalletStorageError: Error {
  case notFound
}

class BTMWalletStorageImpl: CoreDataStorage<BTMWalletStorageUtils>, BTMWalletStorage {
    private let didCoinsChange = PublishRelay<Void>()
    var coinChanged: Observable<Void> {
        return didCoinsChange.asObservable()
    }
    
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
    return save(transaction: {
      try $0.changeVisibility(of: coin)
    }) { [weak self] in
        self?.didCoinsChange.accept(())
    }
  }
  
  func changeIndex(of type: CustomCoinType, with index: Int) -> Completable {
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
      throw BTMWalletStorageError.notFound
    }
    
    return try converter.convert(model: walletRecord)
  }
  
  func changeVisibility(of coin: BTMCoin) throws {
    guard let walletRecord = try BTMWalletRecord.fetchFirst(in: context) else {
      throw BTMWalletStorageError.notFound
    }
    
    let coinRecord = walletRecord.coins.first { $0.type == coin.type.code }
    guard let unwrappedCoinRecord = coinRecord else {
      throw BTMWalletStorageError.notFound
    }
    
    unwrappedCoinRecord.visible.toggle()
  }
  
  func changeIndex(of type: CustomCoinType, with index: Int) throws {
    guard let walletRecord = try BTMWalletRecord.fetchFirst(in: context) else {
      throw BTMWalletStorageError.notFound
    }
    
    let coinRecord = walletRecord.coins.first { $0.type == type.code }
    guard let unwrappedCoinRecord = coinRecord else {
      throw BTMWalletStorageError.notFound
    }
    
    unwrappedCoinRecord.index = Int32(index)
  }
  
  func delete() throws {
    try BTMWalletRecord.fetchAndDelete(in: context)
  }
  
}

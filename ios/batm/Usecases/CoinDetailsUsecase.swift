import Foundation
import RxSwift
import TrustWalletCore

protocol CoinDetailsUsecase {
  func getTransactions(for type: CoinType, from page: Int) -> Single<Transactions>
  func getCoin(for type: CoinType) -> Single<BTMCoin>
  func withdraw(from coin: BTMCoin, to destination: String, amount: Double) -> Completable
  func requestCode() -> Completable
  func verifyCode(code: String) -> Completable
}

class CoinDetailsUsecaseImpl: CoinDetailsUsecase {
  
  let api: APIGateway
  let accountStorage: AccountStorage
  let walletStorage: BTMWalletStorage
  let walletService: WalletService
  
  init(api: APIGateway,
       accountStorage: AccountStorage,
       walletStorage: BTMWalletStorage,
       walletService: WalletService) {
    self.api = api
    self.accountStorage = accountStorage
    self.walletStorage = walletStorage
    self.walletService = walletService
  }
  
  func getTransactions(for type: CoinType, from page: Int) -> Single<Transactions> {
    return accountStorage.get()
      .flatMap { [api] in api.getTransactions(userId: $0.userId, type: type, page: page) }
  }
  
  func getCoin(for type: CoinType) -> Single<BTMCoin> {
    return walletStorage.get()
      .map {
        let coin = $0.coins.first { $0.type == type }
        
        guard let unwrappedCoin = coin  else {
          throw StorageError.notFound
        }
        
        return unwrappedCoin
      }
  }
  
  func withdraw(from coin: BTMCoin, to destination: String, amount: Double) -> Completable {
    return walletService.getTransactionHex(for: coin, destination: destination, amount: amount)
  }
  
  func requestCode() -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [api] in api.requestCode(userId: $0.userId) }
  }
  
  func verifyCode(code: String) -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [api] in api.verifyCode(userId: $0.userId, code: code) }
  }
  
}

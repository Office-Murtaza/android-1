import Foundation
import RxSwift
import TrustWalletCore

protocol CoinDetailsUsecase {
  func getTransactions(for type: CoinType, from page: Int) -> Single<Transactions>
  func getCoin(for type: CoinType) -> Single<BTMCoin>
  func requestCode() -> Completable
  func verifyCode(code: String) -> Completable
  func withdraw(from coin: BTMCoin, to destination: String, amount: Double) -> Completable
  func sendGift(from coin: BTMCoin,
                to phone: String,
                amount: Double,
                message: String,
                imageUrl: String?) -> Completable
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
  
  func requestCode() -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [api] in api.requestCode(userId: $0.userId) }
  }
  
  func verifyCode(code: String) -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [api] in api.verifyCode(userId: $0.userId, code: code) }
  }
  
  func withdraw(from coin: BTMCoin, to destination: String, amount: Double) -> Completable {
    return accountStorage.get()
      .flatMap { [walletService] account in
        return walletService.getTransactionHex(for: coin, destination: destination, amount: amount)
          .map { (account, $0) }
      }
      .flatMapCompletable { [unowned self] account, transactionResultString in
        return self.submit(userId: account.userId,
                           type: coin.type,
                           txType: .withdraw,
                           amount: amount,
                           transactionResultString: transactionResultString)
      }
  }
  
  func sendGift(from coin: BTMCoin,
                to phone: String,
                amount: Double,
                message: String,
                imageUrl: String?) -> Completable {
    return accountStorage.get()
      .flatMap { [api] account in
        return api.getGiftAddress(userId: account.userId, type: coin.type, phone: phone)
          .map { (account, $0.address) }
      }
      .flatMap { [walletService] account, destination in
        return walletService.getTransactionHex(for: coin, destination: destination, amount: amount)
          .map { (account, $0) }
      }
      .flatMapCompletable { [unowned self] account, transactionResultString in
        return self.submit(userId: account.userId,
                           type: coin.type,
                           txType: .sendGift,
                           amount: amount,
                           phone: phone,
                           message: message,
                           imageUrl: imageUrl,
                           transactionResultString: transactionResultString)
      }
  }
  
  private func submit(userId: Int,
                      type: CoinType,
                      txType: TransactionType,
                      amount: Double,
                      phone: String? = nil,
                      message: String? = nil,
                      imageUrl: String? = nil,
                      transactionResultString: String) -> Completable {
    var txhex: String?
    var trxJson: [String: Any]?
    
    switch type {
    case .tron:
      if let data = transactionResultString.data(using: .utf8) {
        trxJson = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
      }
    default: txhex = transactionResultString
    }
    
    return api.submitTransaction(userId: userId,
                                 type: type,
                                 txType: txType,
                                 amount: amount,
                                 phone: phone,
                                 message: message,
                                 imageUrl: imageUrl,
                                 txhex: txhex,
                                 trxJson: trxJson)
  }
  
}

import Foundation
import RxSwift
import TrustWalletCore

protocol CoinDetailsUsecase {
  func getTransactions(for type: CoinType, from page: Int) -> Single<Transactions>
  func getTransactionDetails(for type: CoinType, by txid: String) -> Single<TransactionDetails>
  func getCoin(for type: CoinType) -> Single<BTMCoin>
  func requestCode() -> Completable
  func verifyCode(code: String) -> Completable
  func withdraw(from coin: BTMCoin, to destination: String, amount: Double) -> Completable
  func getSellDetails(for type: CoinType) -> Single<SellDetails>
  func presubmit(for type: CoinType, coinAmount: Double, currencyAmount: Double) -> Single<PreSubmitResponse>
  func sell(from coin: BTMCoin, amount: Double, to toAddress: String) -> Completable
  func sendGift(from coin: BTMCoin,
                to phone: String,
                amount: Double,
                message: String,
                imageId: String?) -> Completable
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
  
  func getTransactionDetails(for type: CoinType, by txid: String) -> Single<TransactionDetails> {
    return accountStorage.get()
      .flatMap { [api] in api.getTransactionDetails(userId: $0.userId, type: type, txid: txid) }
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
                imageId: String?) -> Completable {
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
                           imageId: imageId,
                           transactionResultString: transactionResultString)
      }
  }
  
  func getSellDetails(for type: CoinType) -> Single<SellDetails> {
    return accountStorage.get()
      .flatMap { [api] in api.getSellDetails(userId: $0.userId, type: type) }
//      .map { _ in SellDetails(dailyLimit: 10000, transactionLimit: 3000, profitRate: 1.025) }
  }
  
  func presubmit(for type: CoinType, coinAmount: Double, currencyAmount: Double) -> Single<PreSubmitResponse> {
    return accountStorage.get()
      .flatMap { [api] in api.presubmitTransaction(userId: $0.userId,
                                                   type: type,
                                                   coinAmount: coinAmount,
                                                   currencyAmount: currencyAmount) }
  }
  
  func sell(from coin: BTMCoin, amount: Double, to toAddress: String) -> Completable {
    return accountStorage.get()
      .flatMap { [walletService] account in
        return walletService.getTransactionHex(for: coin, destination: toAddress, amount: amount)
          .map { (account, $0) }
      }
      .flatMapCompletable { [unowned self] account, transactionResultString in
        return self.submit(userId: account.userId,
                           type: coin.type,
                           txType: .sell,
                           amount: amount,
                           transactionResultString: transactionResultString)
      }
  }
  
  private func submit(userId: Int,
                      type: CoinType,
                      txType: TransactionType,
                      amount: Double,
                      phone: String? = nil,
                      message: String? = nil,
                      imageId: String? = nil,
                      transactionResultString: String? = nil) -> Completable {
    var txhex: String?
    var trxJson: [String: Any]?
    
    switch type {
    case .tron:
      if let data = transactionResultString?.data(using: .utf8) {
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
                                 imageId: imageId,
                                 txhex: txhex,
                                 trxJson: trxJson)
  }
  
}

import Foundation
import RxSwift
import TrustWalletCore

protocol CoinDetailsUsecase {
  func getTransactions(for type: CustomCoinType, from page: Int) -> Single<Transactions>
  func getTransactionDetails(for type: CustomCoinType, by id: String) -> Single<TransactionDetails>
  func getCoin(for type: CustomCoinType) -> Single<BTMCoin>
  func requestCode() -> Completable
  func verifyCode(code: String) -> Completable
  func withdraw(from coin: BTMCoin,
                with coinSettings: CoinSettings,
                to destination: String,
                amount: Double) -> Completable
  func getSellDetails(for type: CustomCoinType) -> Single<SellDetails>
  func presubmit(for type: CustomCoinType, coinAmount: Double, currencyAmount: Double) -> Single<PreSubmitResponse>
  func sell(from coin: BTMCoin, with coinSettings: CoinSettings, amount: Double, to toAddress: String) -> Completable
  func sendGift(from coin: BTMCoin,
                with coinSettings: CoinSettings,
                to phone: String,
                amount: Double,
                message: String,
                imageId: String?) -> Completable
  func exchange(from fromCoin: BTMCoin,
                with coinSettings: CoinSettings,
                to toCoinType: CustomCoinType,
                amount: Double) -> Completable
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
  
  func getTransactions(for type: CustomCoinType, from page: Int) -> Single<Transactions> {
    return accountStorage.get()
      .flatMap { [api] in api.getTransactions(userId: $0.userId, type: type, page: page) }
  }
  
  func getTransactionDetails(for type: CustomCoinType, by id: String) -> Single<TransactionDetails> {
    return accountStorage.get()
      .flatMap { [api] in api.getTransactionDetails(userId: $0.userId, type: type, id: id) }
  }
  
  func getCoin(for type: CustomCoinType) -> Single<BTMCoin> {
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
  
  func withdraw(from coin: BTMCoin,
                with coinSettings: CoinSettings,
                to destination: String,
                amount: Double) -> Completable {
    return accountStorage.get()
      .flatMap { [walletService] account in
        return walletService.getTransactionHex(for: coin, with: coinSettings, destination: destination, amount: amount)
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
                with coinSettings: CoinSettings,
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
        return walletService.getTransactionHex(for: coin, with: coinSettings, destination: destination, amount: amount)
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
  
  func getSellDetails(for type: CustomCoinType) -> Single<SellDetails> {
    return accountStorage.get()
      .flatMap { [api] in api.getSellDetails(userId: $0.userId, type: type) }
  }
  
  func presubmit(for type: CustomCoinType, coinAmount: Double, currencyAmount: Double) -> Single<PreSubmitResponse> {
    return accountStorage.get()
      .flatMap { [api] in api.presubmitTransaction(userId: $0.userId,
                                                   type: type,
                                                   coinAmount: coinAmount,
                                                   currencyAmount: currencyAmount) }
  }
  
  func sell(from coin: BTMCoin, with coinSettings: CoinSettings, amount: Double, to toAddress: String) -> Completable {
    return accountStorage.get()
      .flatMap { [walletService] account in
        return walletService.getTransactionHex(for: coin, with: coinSettings, destination: toAddress, amount: amount)
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
  
  func exchange(from fromCoin: BTMCoin, with coinSettings: CoinSettings, to toCoinType: CustomCoinType, amount: Double) -> Completable {
    return accountStorage.get()
      .flatMap { [walletService] account in
        return walletService.getTransactionHex(for: fromCoin,
                                               with: coinSettings,
                                               destination: coinSettings.walletAddress,
                                               amount: amount)
          .map { (account, $0) }
      }
      .flatMapCompletable { [unowned self] account, transactionResultString in
        return self.submit(userId: account.userId,
                           type: fromCoin.type,
                           txType: .sendC2C,
                           amount: amount,
                           toCoinType: toCoinType,
                           transactionResultString: transactionResultString)
      }
  }
  
  private func submit(userId: Int,
                      type: CustomCoinType,
                      txType: TransactionType,
                      amount: Double,
                      phone: String? = nil,
                      message: String? = nil,
                      imageId: String? = nil,
                      toCoinType: CustomCoinType? = nil,
                      transactionResultString: String? = nil) -> Completable {
    
    return api.submitTransaction(userId: userId,
                                 type: type,
                                 txType: txType,
                                 amount: amount,
                                 phone: phone,
                                 message: message,
                                 imageId: imageId,
                                 toCoinType: toCoinType,
                                 txhex: transactionResultString)
  }
  
}

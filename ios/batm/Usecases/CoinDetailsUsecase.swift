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
  func getSellDetails() -> Single<SellDetails>
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
  func reserve(from coin: BTMCoin, with coinSettings: CoinSettings, amount: Double) -> Completable
  func recall(from coin: BTMCoin, amount: Double) -> Completable
  func getStakeDetails(for type: CustomCoinType) -> Single<StakeDetails>
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
                           fee: coinSettings.txFee,
                           fromAddress: coin.address,
                           toAddress: destination,
                           transactionResultString: transactionResultString)
      }
  }
  
  func sendGift(from coin: BTMCoin,
                with coinSettings: CoinSettings,
                to phone: String,
                amount: Double,
                message: String,
                imageId: String?) -> Completable {
    return api.getGiftAddress(type: coin.type, phone: phone)
      .map { $0.address }
      .flatMap { [walletService] destination in
        return walletService.getTransactionHex(for: coin, with: coinSettings, destination: destination, amount: amount)
          .map { ($0, destination) }
      }
      .flatMap { [accountStorage] transactionResultString, toAddress in
        return accountStorage.get()
          .map { ($0, transactionResultString, toAddress) }
      }
      .flatMapCompletable { [unowned self] account, transactionResultString, toAddress in
        return self.submit(userId: account.userId,
                         type: coin.type,
                         txType: .sendGift,
                         amount: amount,
                         fee: coinSettings.txFee,
                         fromAddress: coin.address,
                         toAddress: toAddress,
                         phone: phone,
                         message: message,
                         imageId: imageId,
                           transactionResultString: transactionResultString)
      }
  }
  
  func getSellDetails() -> Single<SellDetails> {
    return accountStorage.get()
      .flatMap { [api] in api.getSellDetails(userId: $0.userId) }
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
                           fee: coinSettings.txFee,
                           fromAddress: coin.address,
                           toAddress: toAddress,
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
                           fee: coinSettings.txFee,
                           fromAddress: fromCoin.address,
                           toAddress: coinSettings.walletAddress,
                           toCoinType: toCoinType,
                           transactionResultString: transactionResultString)
      }
  }
  
  func reserve(from coin: BTMCoin, with coinSettings: CoinSettings, amount: Double) -> Completable {
    return accountStorage.get()
      .flatMap { [walletService] account in
        return walletService.getTransactionHex(for: coin,
                                               with: coinSettings,
                                               destination: coinSettings.walletAddress,
                                               amount: amount)
          .map { (account, $0) }
      }
      .flatMapCompletable { [unowned self] account, transactionResultString in
        return self.submit(userId: account.userId,
                           type: coin.type,
                           txType: .reserve,
                           amount: amount,
                           fee: coinSettings.txFee,
                           fromAddress: coin.address,
                           toAddress: coinSettings.walletAddress,
                           transactionResultString: transactionResultString)
      }
  }
  
  func recall(from coin: BTMCoin, amount: Double) -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [unowned self] account in
        return self.submit(userId: account.userId,
                           type: coin.type,
                           txType: .recall,
                           amount: amount)
      }
  }
  
  private func submit(userId: Int,
                      type: CustomCoinType,
                      txType: TransactionType,
                      amount: Double,
                      fee: Double? = nil,
                      fromAddress: String? = nil,
                      toAddress: String? = nil,
                      phone: String? = nil,
                      message: String? = nil,
                      imageId: String? = nil,
                      toCoinType: CustomCoinType? = nil,
                      transactionResultString: String? = nil) -> Completable {
    
    return api.submitTransaction(userId: userId,
                                 type: type,
                                 txType: txType,
                                 amount: amount,
                                 fee: fee,
                                 fromAddress: fromAddress,
                                 toAddress: toAddress,
                                 phone: phone,
                                 message: message,
                                 imageId: imageId,
                                 toCoinType: toCoinType,
                                 txhex: transactionResultString)
  }
  
  func getStakeDetails(for type: CustomCoinType) -> Single<StakeDetails> {
    return accountStorage.get()
      .flatMap { [api] in api.getStakeDetails(userId: $0.userId, type: type) }
  }
  
}

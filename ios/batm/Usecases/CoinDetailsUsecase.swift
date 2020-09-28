import Foundation
import RxSwift
import TrustWalletCore

protocol CoinDetailsUsecase {
  func getTransactions(for type: CustomCoinType, from page: Int) -> Single<Transactions>
  func getTransactionDetails(for type: CustomCoinType, by id: String) -> Single<TransactionDetails>
  func getCoin(for type: CustomCoinType) -> Single<BTMCoin>
  func verifyCode(code: String) -> Completable
  func withdraw(from coin: BTMCoin,
                with coinSettings: CoinSettings,
                to destination: String,
                amount: Decimal) -> Completable
  func getSellDetails() -> Single<SellDetails>
  func presubmit(for type: CustomCoinType, coinAmount: Decimal, currencyAmount: Decimal) -> Single<PreSubmitResponse>
  func sell(from coin: BTMCoin, with coinSettings: CoinSettings, amount: Decimal, to toAddress: String) -> Completable
  func sendGift(from coin: BTMCoin,
                with coinSettings: CoinSettings,
                to phone: String,
                amount: Decimal,
                message: String,
                imageId: String?) -> Completable
  func exchange(from fromCoin: BTMCoin,
                with coinSettings: CoinSettings,
                to toCoinType: CustomCoinType,
                amount: Decimal,
                toCoinAmount: Decimal) -> Completable
  func reserve(from coin: BTMCoin, with coinSettings: CoinSettings, amount: Decimal) -> Completable
  func recall(from coin: BTMCoin, amount: Decimal) -> Completable
  func getStakeDetails(for type: CustomCoinType) -> Single<StakeDetails>
  func stake(from coin: BTMCoin, with coinSettings: CoinSettings, amount: Decimal) -> Completable
  func unstake(from coin: BTMCoin, with coinSettings: CoinSettings, stakeDetails: StakeDetails) -> Completable
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
  
  func verifyCode(code: String) -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [api] in api.verifyCode(userId: $0.userId, code: code) }
  }
  
  func withdraw(from coin: BTMCoin,
                with coinSettings: CoinSettings,
                to destination: String,
                amount: Decimal) -> Completable {
    return Single.just(coin.type)
      .flatMap { [api] type -> Single<Void> in
        if type != .ripple {
          return .just(())
        }
        
        return api.getCurrentAccountActivated(type: type, address: destination)
          .map { isActivated in
            if !isActivated && amount < 20 {
              throw APIError.serverError(ServerError(code: 3, message: localize(L.CoinWithdraw.Form.Error.notEnoughToActivate)))
            }
            
            return Void()
          }
      }
      .flatMap { [accountStorage] in accountStorage.get() }
      .flatMap { [walletService] account in
        return walletService.getTransactionHex(for: coin,
                                               with: coinSettings,
                                               destination: destination,
                                               amount: amount,
                                               stake: nil)
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
                amount: Decimal,
                message: String,
                imageId: String?) -> Completable {
    return api.getGiftAddress(type: coin.type, phone: phone)
      .map { $0.address }
      .flatMap { [walletService] destination in
        return walletService.getTransactionHex(for: coin,
                                               with: coinSettings,
                                               destination: destination,
                                               amount: amount,
                                               stake: nil)
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
  
  func presubmit(for type: CustomCoinType, coinAmount: Decimal, currencyAmount: Decimal) -> Single<PreSubmitResponse> {
    return accountStorage.get()
      .flatMap { [api] in api.presubmitTransaction(userId: $0.userId,
                                                   type: type,
                                                   coinAmount: coinAmount,
                                                   currencyAmount: currencyAmount) }
  }
  
  func sell(from coin: BTMCoin, with coinSettings: CoinSettings, amount: Decimal, to toAddress: String) -> Completable {
    return accountStorage.get()
      .flatMap { [walletService] account in
        return walletService.getTransactionHex(for: coin,
                                               with: coinSettings,
                                               destination: toAddress,
                                               amount: amount,
                                               stake: nil)
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
  
  func exchange(from fromCoin: BTMCoin,
                with coinSettings: CoinSettings,
                to toCoinType: CustomCoinType,
                amount: Decimal,
                toCoinAmount: Decimal) -> Completable {
    return accountStorage.get()
      .flatMap { [walletService] account in
        return walletService.getTransactionHex(for: fromCoin,
                                               with: coinSettings,
                                               destination: coinSettings.walletAddress,
                                               amount: amount,
                                               stake: nil)
          .map { (account, $0) }
      }
      .flatMapCompletable { [unowned self] account, transactionResultString in
        return self.submit(userId: account.userId,
                           type: fromCoin.type,
                           txType: .sendС2С,
                           amount: amount,
                           fee: coinSettings.txFee,
                           fromAddress: fromCoin.address,
                           toAddress: coinSettings.contractAddress,
                           toCoinType: toCoinType,
                           toCoinAmount: toCoinAmount,
                           transactionResultString: transactionResultString)
      }
  }
  
  func reserve(from coin: BTMCoin, with coinSettings: CoinSettings, amount: Decimal) -> Completable {
    return accountStorage.get()
      .flatMap { [walletService] account in
        return walletService.getTransactionHex(for: coin,
                                               with: coinSettings,
                                               destination: coinSettings.walletAddress,
                                               amount: amount,
                                               stake: nil)
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
  
  func recall(from coin: BTMCoin, amount: Decimal) -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [unowned self] account in
        return self.submit(userId: account.userId,
                           type: coin.type,
                           txType: .recall,
                           amount: amount)
      }
  }
  
  func stake(from coin: BTMCoin, with coinSettings: CoinSettings, amount: Decimal) -> Completable {
    return accountStorage.get()
      .flatMap { [walletService] account in
        return walletService.getTransactionHex(for: coin,
                                               with: coinSettings,
                                               destination: "",
                                               amount: amount,
                                               stake: true)
          .map { (account, $0) }
      }
      .flatMapCompletable { [unowned self] account, transactionResultString in
        return self.submit(userId: account.userId,
                           type: coin.type,
                           txType: .stake,
                           amount: amount,
                           fee: coinSettings.txFee,
                           fromAddress: coin.address,
                           toAddress: coinSettings.contractAddress,
                           transactionResultString: transactionResultString)
      }
  }
  
  func unstake(from coin: BTMCoin, with coinSettings: CoinSettings, stakeDetails: StakeDetails) -> Completable {
    return accountStorage.get()
      .flatMap { [walletService] account in
        return walletService.getTransactionHex(for: coin,
                                               with: coinSettings,
                                               destination: "",
                                               amount: 0,
                                               stake: false)
          .map { (account, $0) }
      }
      .flatMapCompletable { [unowned self] account, transactionResultString in
        return self.submit(userId: account.userId,
                           type: coin.type,
                           txType: .unstake,
                           amount: (stakeDetails.amount ?? 0) + (stakeDetails.rewardAmount ?? 0),
                           fee: coinSettings.txFee,
                           fromAddress: coin.address,
                           toAddress: coinSettings.contractAddress,
                           transactionResultString: transactionResultString)
      }
  }
  
  private func submit(userId: Int,
                      type: CustomCoinType,
                      txType: TransactionType,
                      amount: Decimal,
                      fee: Decimal? = nil,
                      fromAddress: String? = nil,
                      toAddress: String? = nil,
                      phone: String? = nil,
                      message: String? = nil,
                      imageId: String? = nil,
                      toCoinType: CustomCoinType? = nil,
                      toCoinAmount: Decimal? = nil,
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
                                 toCoinAmount: toCoinAmount,
                                 txhex: transactionResultString)
  }
  
  func getStakeDetails(for type: CustomCoinType) -> Single<StakeDetails> {
    return accountStorage.get()
      .flatMap { [api] in api.getStakeDetails(userId: $0.userId, type: type) }
  }
  
}

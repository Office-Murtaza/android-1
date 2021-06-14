import Foundation
import RxSwift
import TrustWalletCore

protocol CoinDetailsUsecase {
    func getTransactions(for type: CustomCoinType, from page: Int) -> Single<Transactions>
    func getTransactionDetails(for type: CustomCoinType) -> Observable<TransactionDetails?>
    func removeTransactionDetails()
    func getCoin(for type: CustomCoinType) -> Single<BTMCoin>
    func verifyCode(code: String) -> Completable
    func withdraw(from coin: BTMCoin,
                  with coinDetails: CoinDetails,
                  to destination: String,
                  amount: Decimal) -> Single<TransactionDetails>
    func getSellDetails() -> Single<SellDetails>
    func presubmit(for type: CustomCoinType, coinAmount: Decimal, currencyAmount: Decimal) -> Single<PreSubmitResponse>
    func sell(from coin: BTMCoin, with coinDetails: CoinDetails, amount: Decimal, to toAddress: String) -> Completable
    func sendGift(from coin: BTMCoin,
                  with coinDetails: CoinDetails,
                  to phone: String,
                  amount: Decimal,
                  message: String,
                  image: String?) -> Single<TransactionDetails>
    func exchange(from fromCoin: BTMCoin,
                  with coinDetails: CoinDetails,
                  to toCoinType: CustomCoinType,
                  amount: Decimal,
                  toCoinAmount: Decimal) -> Single<TransactionDetails>
    func reserve(from coin: BTMCoin, with coinDetails: CoinDetails, amount: Decimal) -> Single<TransactionDetails>
    func recall(from coin: BTMCoin, amount: Decimal) -> Single<TransactionDetails>
    func getStakeDetails(for type: CustomCoinType) -> Single<StakeDetails>
    func createStake(from coin: BTMCoin, with coinDetails: CoinDetails, amount: Decimal) -> Completable
    func cancelStake(from coin: BTMCoin, with coinDetails: CoinDetails, stakeDetails: StakeDetails) -> Completable
    func withdrawStake(from coin: BTMCoin, with coinDetails: CoinDetails, stakeDetails: StakeDetails) -> Completable
    func getCoinDetails(for type: CustomCoinType) -> Observable<CoinDetails?>
    func getCoinsBalance() -> Observable<CoinsBalance>
    func getCoinActivatedState(for coin: BTMCoin) -> Single<Bool>
}

class CoinDetailsUsecaseImpl: CoinDetailsUsecase {
    
    let api: APIGateway
    let accountStorage: AccountStorage
    let walletStorage: BTMWalletStorage
    let walletService: WalletService
    let balanceService: BalanceService
    let transactionService: TransactionDetailsService
    
    init(api: APIGateway,
         accountStorage: AccountStorage,
         walletStorage: BTMWalletStorage,
         walletService: WalletService,
         balanceService: BalanceService,
         transactionService: TransactionDetailsService) {
        self.api = api
        self.accountStorage = accountStorage
        self.walletStorage = walletStorage
        self.walletService = walletService
        self.balanceService = balanceService
        self.transactionService = transactionService
    }
    
    func getTransactions(for type: CustomCoinType, from page: Int) -> Single<Transactions> {
        return accountStorage.get()
            .flatMap { [api] in api.getTransactions(userId: $0.userId, type: type, page: page) }
    }
    
    func getTransactionDetails(for type: CustomCoinType) -> Observable<TransactionDetails?> {
        return transactionService.getTransactionDetails()
    }
    
    func removeTransactionDetails() {
        transactionService.removeTransactionDetails()
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
                  with coinDetails: CoinDetails,
                  to destination: String,
                  amount: Decimal) -> Single<TransactionDetails> {
        return Single.just(coin.type)
            .flatMap { [api] type -> Single<Void> in
                if type != .ripple {
                    return .just(())
                }
                
                return api.getCurrentAccountActivated(address: destination)
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
                                                       with: coinDetails,
                                                       destination: destination,
                                                       amount: amount,
                                                       stakingType: nil)
                    .map { (account, $0) }
            }
            .flatMap { [unowned self] account, transactionResultString in
                return self.coinDetailsSubmit(userId: account.userId,
                                              type: coin.type,
                                              txType: .withdraw,
                                              amount: amount,
                                              fee: coinDetails.txFee,
                                              fromAddress: coin.address,
                                              toAddress: destination,
                                              transactionResultString: transactionResultString,
                                              from: .withdraw)
                
            }
    }
    
    func getCoinActivatedState(for coin: BTMCoin) -> Single<Bool> {
        return api.getCurrentAccountActivated(address: coin.address)
    }
    
    func sendGift(from coin: BTMCoin,
                  with coinDetails: CoinDetails,
                  to phone: String,
                  amount: Decimal,
                  message: String,
                  image: String?) -> Single<TransactionDetails> {
        return api.getGiftAddress(type: coin.type, phone: phone)
            .map { $0.address }
            .flatMap { [walletService] destination in
                return walletService.getTransactionHex(for: coin,
                                                       with: coinDetails,
                                                       destination: destination,
                                                       amount: amount,
                                                       stakingType: nil)
                    .map { ($0, destination) }
            }
            .flatMap { [accountStorage] transactionResultString, toAddress in
                return accountStorage.get()
                    .map { ($0, transactionResultString, toAddress) }
            }
            .flatMap { [unowned self] account, transactionResultString, toAddress in
                return self.coinDetailsSubmit(userId: account.userId,
                                              type: coin.type,
                                              txType: .sendTransfer,
                                              amount: amount,
                                              fee: coinDetails.txFee,
                                              fromAddress: coin.address,
                                              toAddress: toAddress,
                                              phone: phone,
                                              message: message,
                                              image: image,
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
    
    func sell(from coin: BTMCoin, with coinDetails: CoinDetails, amount: Decimal, to toAddress: String) -> Completable {
        return accountStorage.get()
            .flatMap { [walletService] account in
                return walletService.getTransactionHex(for: coin,
                                                       with: coinDetails,
                                                       destination: toAddress,
                                                       amount: amount,
                                                       stakingType: nil)
                    .map { (account, $0) }
            }
            .flatMapCompletable { [unowned self] account, transactionResultString in
                return self.submit(userId: account.userId,
                                   type: coin.type,
                                   txType: .sell,
                                   amount: amount,
                                   fee: coinDetails.txFee,
                                   fromAddress: coin.address,
                                   toAddress: toAddress,
                                   transactionResultString: transactionResultString)
            }
    }
    
    func exchange(from fromCoin: BTMCoin,
                  with coinDetails: CoinDetails,
                  to toCoinType: CustomCoinType,
                  amount: Decimal,
                  toCoinAmount: Decimal) -> Single<TransactionDetails> {
        return accountStorage.get()
            .flatMap { [walletService] account in
                return walletService.getTransactionHex(for: fromCoin,
                                                       with: coinDetails,
                                                       destination: coinDetails.walletAddress.value,
                                                       amount: amount,
                                                       stakingType: nil)
                    .map { (account, $0) }
            }
            .flatMap { [unowned self] account, transactionResultString in
                return self.coinDetailsSubmit(userId: account.userId,
                                              type: fromCoin.type,
                                              txType: .sendSwap,
                                              amount: amount,
                                              fee: coinDetails.txFee,
                                              fromAddress: fromCoin.address,
                                              toAddress: fromCoin.type.isETHBased ? coinDetails.contractAddress : coinDetails.walletAddress,
                                              toCoinType: toCoinType,
                                              toCoinAmount: toCoinAmount,
                                              transactionResultString: transactionResultString)
            }
    }
    
    func reserve(from coin: BTMCoin, with coinDetails: CoinDetails, amount: Decimal) -> Single<TransactionDetails> {
        return accountStorage.get()
            .flatMap { [walletService] account in
                return walletService.getTransactionHex(for: coin,
                                                       with: coinDetails,
                                                       destination: coinDetails.walletAddress.value,
                                                       amount: amount,
                                                       stakingType: nil)
                    .map { (account, $0) }
            }
            .flatMap { [unowned self] account, transactionResultString in
                return self.coinDetailsSubmit(userId: account.userId,
                                              type: coin.type,
                                              txType: .reserve,
                                              amount: amount,
                                              fee: coinDetails.txFee,
                                              fromAddress: coin.address,
                                              toAddress: coinDetails.walletAddress,
                                              transactionResultString: transactionResultString,
                                              from: .reserve)
                
            }
    }
    
    func recall(from coin: BTMCoin, amount: Decimal) -> Single<TransactionDetails> {
        return accountStorage.get()
            .flatMap { [unowned self] account in
                return self.coinDetailsSubmit(userId: account.userId,
                                              type: coin.type,
                                              txType: .recall,
                                              amount: amount,
                                              from: .recall)
                
            }
    }
    
    func createStake(from coin: BTMCoin, with coinDetails: CoinDetails, amount: Decimal) -> Completable {
        return accountStorage.get()
            .flatMap { [walletService] account in
                return walletService.getTransactionHex(for: coin,
                                                       with: coinDetails,
                                                       destination: "",
                                                       amount: amount,
                                                       stakingType: .createStake)
                    .map { (account, $0) }
            }
            .flatMapCompletable { [unowned self] account, transactionResultString in
                return self.submit(userId: account.userId,
                                   type: coin.type,
                                   txType: .createStake,
                                   amount: amount,
                                   fee: coinDetails.txFee,
                                   fromAddress: coin.address,
                                   toAddress: coinDetails.contractAddress,
                                   transactionResultString: transactionResultString)
            }
    }
    
    func cancelStake(from coin: BTMCoin, with coinDetails: CoinDetails, stakeDetails: StakeDetails) -> Completable {
        return accountStorage.get()
            .flatMap { [walletService] account in
                return walletService.getTransactionHex(for: coin,
                                                       with: coinDetails,
                                                       destination: "",
                                                       amount: 0,
                                                       stakingType: .cancelStake)
                    .map { (account, $0) }
            }
            .flatMapCompletable { [unowned self] account, transactionResultString in
                return self.submit(userId: account.userId,
                                   type: coin.type,
                                   txType: .cancelStake,
                                   amount: 0,
                                   fee: coinDetails.txFee,
                                   fromAddress: coin.address,
                                   toAddress: coinDetails.contractAddress,
                                   transactionResultString: transactionResultString)
            }
    }
    
    func withdrawStake(from coin: BTMCoin, with coinDetails: CoinDetails, stakeDetails: StakeDetails) -> Completable {
        return accountStorage.get()
            .flatMap { [walletService] account in
                return walletService.getTransactionHex(for: coin,
                                                       with: coinDetails,
                                                       destination: "",
                                                       amount: 0,
                                                       stakingType: .withdrawStake)
                    .map { (account, $0) }
            }
            .flatMapCompletable { [unowned self] account, transactionResultString in
                return self.submit(userId: account.userId,
                                   type: coin.type,
                                   txType: .withdrawStake,
                                   amount: (Decimal(stakeDetails.cryptoAmount ?? 0)) + (Decimal(stakeDetails.rewardAmount )),
                                   fee: coinDetails.txFee,
                                   fromAddress: coin.address,
                                   toAddress: coinDetails.contractAddress,
                                   transactionResultString: transactionResultString)
            }
    }
    
    private func coinDetailsSubmit(userId: String,
                                   type: CustomCoinType,
                                   txType: TransactionType,
                                   amount: Decimal,
                                   fee: Decimal? = nil,
                                   fromAddress: String? = nil,
                                   toAddress: String? = nil,
                                   phone: String? = nil,
                                   message: String? = nil,
                                   image: String? = nil,
                                   toCoinType: CustomCoinType? = nil,
                                   toCoinAmount: Decimal? = nil,
                                   transactionResultString: String? = nil,
                                   from screen: ScreenType = .none) -> Single<TransactionDetails> {
        
        return api.submitCoinTransaction(userId: userId,
                                         type: type,
                                         txType: txType,
                                         amount: amount,
                                         fee: fee,
                                         fromAddress: fromAddress,
                                         toAddress: toAddress,
                                         phone: phone,
                                         message: message,
                                         image: image,
                                         toCoinType: toCoinType,
                                         toCoinAmount: toCoinAmount,
                                         txhex: transactionResultString,
                                         from: screen)
    }
    
    
    
    private func submit(userId: String,
                        type: CustomCoinType,
                        txType: TransactionType,
                        amount: Decimal,
                        fee: Decimal? = nil,
                        fromAddress: String? = nil,
                        toAddress: String? = nil,
                        phone: String? = nil,
                        message: String? = nil,
                        image: String? = nil,
                        toCoinType: CustomCoinType? = nil,
                        toCoinAmount: Decimal? = nil,
                        transactionResultString: String? = nil,
                        from screen: ScreenType = .none) -> Completable {
        
        return api.submitTransaction(userId: userId,
                                     type: type,
                                     txType: txType,
                                     amount: amount,
                                     fee: fee,
                                     fromAddress: fromAddress,
                                     toAddress: toAddress,
                                     phone: phone,
                                     message: message,
                                     image: image,
                                     toCoinType: toCoinType,
                                     toCoinAmount: toCoinAmount,
                                     txhex: transactionResultString,
                                     from: screen)
    }
    
    func getStakeDetails(for type: CustomCoinType) -> Single<StakeDetails> {
        return accountStorage.get()
            .flatMap { [api] in api.getStakeDetails(userId: $0.userId, type: type) }
    }
    
    func getCoinDetails(for type: CustomCoinType) -> Observable<CoinDetails?> {
        return balanceService.getCoinDetails(for: type)
    }
    
    func getCoinsBalance() -> Observable<CoinsBalance> {
        return balanceService.getCoinsBalance()
    }
}

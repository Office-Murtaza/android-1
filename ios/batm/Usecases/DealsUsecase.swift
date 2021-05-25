import Foundation
import RxSwift

protocol DealsUsecase {
    func exchange(from fromCoin: BTMCoin,
                  with coinDetails: CoinDetails,
                  to toCoinType: CustomCoinType,
                  amount: Decimal,
                  toCoinAmount: Decimal) -> Single<TransactionDetails>
    func getStakeDetails(for type: CustomCoinType) -> Single<StakeDetails>
    func createStake(from coin: BTMCoin, with coinDetails: CoinDetails, amount: Decimal) -> Single<TransactionDetails>
    func cancelStake(from coin: BTMCoin, with coinDetails: CoinDetails, stakeDetails: StakeDetails) -> Single<TransactionDetails>
    func withdrawStake(from coin: BTMCoin, with coinDetails: CoinDetails, stakeDetails: StakeDetails) -> Single<TransactionDetails>
    func getCoinDetails(for type: CustomCoinType) -> Observable<CoinDetails?>
    func getCoinsBalance() -> Observable<CoinsBalance>
    func getCoin(for type: CustomCoinType) -> Single<BTMCoin>
    func getTrades() -> Single<Trades>
    func getAccount() -> Single<Account>
}

class DealsUsecaseImpl: DealsUsecase {
    let api: APIGateway
    let accountStorage: AccountStorage
    let walletService: WalletService
    let walletStorage: BTMWalletStorage
    let balanceService: BalanceService
    
    init(api: APIGateway,
         accountStorage: AccountStorage,
         walletService: WalletService,
         walletStorage: BTMWalletStorage,
         balanceService: BalanceService) {
        self.api = api
        self.accountStorage = accountStorage
        self.walletService = walletService
        self.walletStorage = walletStorage
        self.balanceService = balanceService
    }
    
    func getCoinsBalance() -> Observable<CoinsBalance> {
        return balanceService.getCoinsBalance()
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
    
    func getCoinDetails(for type: CustomCoinType) -> Observable<CoinDetails?> {
        return balanceService.getCoinDetails(for: type)
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
                return self.submit(userId: account.userId,
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
    
    func getStakeDetails(for type: CustomCoinType) -> Single<StakeDetails> {
        return accountStorage.get()
            .flatMap { [api] in api.getStakeDetails(userId: $0.userId, type: type) }
    }
    
    func createStake(from coin: BTMCoin, with coinDetails: CoinDetails, amount: Decimal) -> Single<TransactionDetails> {
        return accountStorage.get()
            .flatMap { [walletService] account in
                return walletService.getTransactionHex(for: coin,
                                                       with: coinDetails,
                                                       destination: "",
                                                       amount: amount,
                                                       stakingType: .createStake)
                    .map { (account, $0) }
            }
            .flatMap { [unowned self] account, transactionResultString in
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
    
    func cancelStake(from coin: BTMCoin, with coinDetails: CoinDetails, stakeDetails: StakeDetails) -> Single<TransactionDetails> {
        return accountStorage.get()
            .flatMap { [walletService] account in
                return walletService.getTransactionHex(for: coin,
                                                       with: coinDetails,
                                                       destination: "",
                                                       amount: 0,
                                                       stakingType: .cancelStake)
                    .map { (account, $0) }
            }
            .flatMap { [unowned self] account, transactionResultString in
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
    
    func withdrawStake(from coin: BTMCoin, with coinDetails: CoinDetails, stakeDetails: StakeDetails) -> Single<TransactionDetails> {
        return accountStorage.get()
            .flatMap { [walletService] account in
                return walletService.getTransactionHex(for: coin,
                                                       with: coinDetails,
                                                       destination: "",
                                                       amount: 0,
                                                       stakingType: .withdrawStake)
                    .map { (account, $0) }
            }
            .flatMap { [unowned self] account, transactionResultString in
                return self.submit(userId: account.userId,
                                   type: coin.type,
                                   txType: .withdrawStake,
                                   amount: (Decimal(stakeDetails.cryptoAmount ?? 0)) + (Decimal(stakeDetails.rewardAmount)),
                                   fee: coinDetails.txFee,
                                   fromAddress: coin.address,
                                   toAddress: coinDetails.contractAddress,
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
                        image: String? = nil,
                        toCoinType: CustomCoinType? = nil,
                        toCoinAmount: Decimal? = nil,
                        transactionResultString: String? = nil) -> Single<TransactionDetails> {
        
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
                                         from: .none)
    }
    
    func getTrades() -> Single<Trades> {
        return accountStorage.get().flatMap{ [api] in api.getTrades(userId: $0.userId)}
    }
    
    func getAccount() -> Single<Account> {
        return accountStorage.get()
    }
}

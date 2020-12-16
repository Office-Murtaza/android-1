import Foundation
import RxSwift

protocol DealsUsecase {
    func exchange(from fromCoin: BTMCoin,
                  with coinDetails: CoinDetails,
                  to toCoinType: CustomCoinType,
                  amount: Decimal,
                  toCoinAmount: Decimal) -> Completable
    func getStakeDetails(for type: CustomCoinType) -> Single<StakeDetails>
    func createStake(from coin: BTMCoin, with coinDetails: CoinDetails, amount: Decimal) -> Completable
    func cancelStake(from coin: BTMCoin, with coinDetails: CoinDetails, stakeDetails: StakeDetails) -> Completable
}

class DealsUsecaseImpl: DealsUsecase {
    let api: APIGateway
    let accountStorage: AccountStorage
    let walletService: WalletService
    
    init(api: APIGateway,
         accountStorage: AccountStorage,
         walletService: WalletService) {
        self.api = api
        self.accountStorage = accountStorage
        self.walletService = walletService
    }
    
    func exchange(from fromCoin: BTMCoin,
                  with coinDetails: CoinDetails,
                  to toCoinType: CustomCoinType,
                  amount: Decimal,
                  toCoinAmount: Decimal) -> Completable {
        return accountStorage.get()
            .flatMap { [walletService] account in
                return walletService.getTransactionHex(for: fromCoin,
                                                       with: coinDetails,
                                                       destination: coinDetails.walletAddress,
                                                       amount: amount,
                                                       stakingType: nil)
                    .map { (account, $0) }
            }
            .flatMapCompletable { [unowned self] account, transactionResultString in
                return self.submit(userId: account.userId,
                                   type: fromCoin.type,
                                   txType: .sendС2С,
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
}

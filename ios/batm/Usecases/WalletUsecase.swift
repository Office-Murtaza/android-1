import Foundation
import RxSwift
import TrustWalletCore

protocol WalletUsecase {
    func getCoinsBalance() -> Observable<CoinsBalance>
    func getCoinDetails(for type: CustomCoinType) -> Observable<CoinDetails?>
    func getPriceChartDetails(for type: CustomCoinType, period: SelectedPeriod) -> Single<PriceChartDetails>
    func getCoins() -> Observable<Void>
    func getCoinsList() -> Single<[BTMCoin]>
    func getTrades() -> Single<Trades>
    func createTrade(data: P2PCreateTradeDataModel) -> Single<Trade>
    func editTrade(data: P2PEditTradeDataModel) -> Single<Trade>
    func cancelTrade(id: String) -> Single<Trade>
    func createOrder(data: P2PCreateOrderDataModel) -> Single<Order>
    
    func removeCoinDetails()
  
  func cancelOrder(id: String) -> Single<Order>
  func updateOrder(id: String, status: OrderDetailsActionType, rate: Int?) -> Single<Order>
  
}

class WalletUsecaseImpl: WalletUsecase, HasDisposeBag {
    let api: APIGateway
    let accountStorage: AccountStorage
    let walletStorage: BTMWalletStorage
    let balanceService: BalanceService
    
    init(api: APIGateway,
         accountStorage: AccountStorage,
         walletStorage: BTMWalletStorage,
         balanceService: BalanceService) {
        self.api = api
        self.accountStorage = accountStorage
        self.walletStorage = walletStorage
        self.balanceService = balanceService
    }
    
    func getCoinsBalance() -> Observable<CoinsBalance> {
        return balanceService.getCoinsBalance()
    }
    
    func getCoinDetails(for type: CustomCoinType) -> Observable<CoinDetails?> {
        return balanceService.getCoinDetails(for: type)
    }
    
    func getPriceChartDetails(for type: CustomCoinType, period: SelectedPeriod) -> Single<PriceChartDetails> {
        return api.getPriceChart(type: type, period: period)
    }
    
    func getCoins() -> Observable<Void> {
        return walletStorage.coinChanged
    }
    
    func getCoinsList() -> Single<[BTMCoin]> {
        return walletStorage.get().map { $0.coins.filter { $0.isVisible }.sorted(by: { $0.index < $1.index }) }
    }
    
    func createTrade(data: P2PCreateTradeDataModel) -> Single<Trade> {
        return accountStorage.get()
            .asObservable()
            .flatMap { [api] user in
                api.createTrade(userId: user.userId, data: data)
            }
            .asSingle()
    }
    
    func editTrade(data: P2PEditTradeDataModel) -> Single<Trade> {
        return accountStorage.get()
            .asObservable()
            .flatMap { [api] user in
                api.editTrade(userId: user.userId, data: data)
            }
            .asSingle()
    }
    
    func cancelTrade(id: String) -> Single<Trade> {
        return accountStorage.get()
            .asObservable()
            .flatMap { [api] user in
                api.cancelTrade(userId: user.userId, id: id)
            }
            .asSingle()
    }
    
    func getTrades() -> Single<Trades> {
        return accountStorage.get().flatMap{ [api] in api.getTrades(userId: $0.userId)}
    }
    
    func removeCoinDetails() {
        balanceService.removeCoinDetails()
    }
    
    func createOrder(data: P2PCreateOrderDataModel) -> Single<Order> {
        return accountStorage.get()
            .asObservable()
            .flatMap { [api] user in
                api.createOrder(userId: user.userId,
                                tradeId: data.tradeId,
                                price: data.price,
                                cryptoAmount: data.cryptoAmount,
                                fiatAmount: data.fiatAmount)
            }
            .asSingle()
    }
  
  func cancelOrder(id: String) -> Single<Order> {
    return accountStorage.get()
        .asObservable()
        .flatMap { [api] user in
          api.cancelOrder(userId: user.userId, id: id)
        }
        .asSingle()
  }
  
  func updateOrder(id: String, status: OrderDetailsActionType, rate: Int?) -> Single<Order> {
    return accountStorage.get()
        .asObservable()
        .flatMap { [api] user in
          api.updateOrder(userId: user.userId, id: id, status: status, rate: rate)
        }
        .asSingle()
  }

}

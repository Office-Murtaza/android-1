import Foundation
import RxSwift
import TrustWalletCore

protocol WalletUsecase {
  func getCoinsBalance(filteredByActive: Bool) -> Single<CoinsBalance>
  func getCoinDetails(for type: CustomCoinType) -> Single<CoinDetails>
  func getPriceChartDetails(for type: CustomCoinType, period: SelectedPeriod) -> Single<PriceChartDetails>
  func getCoins() -> Observable<Void>
  func getCoinsList() -> Single<[BTMCoin]>
  
  func getTrades() -> Single<Trades>
  
  func createTrade(data: P2PCreateTradeDataModel) -> Single<Trade>
  func editTrade(data: P2PEditTradeDataModel) -> Single<Trade>
  func cancelTrade(id: String) -> Single<Trade>
  func createOrder(data: P2PCreateOrderDataModel) -> Single<Order>
}

class WalletUsecaseImpl: WalletUsecase, HasDisposeBag {
  
  let api: APIGateway
  let accountStorage: AccountStorage
  let walletStorage: BTMWalletStorage
  
  init(api: APIGateway,
       accountStorage: AccountStorage,
       walletStorage: BTMWalletStorage) {
    self.api = api
    self.accountStorage = accountStorage
    self.walletStorage = walletStorage
  }
    
    func getCoinsBalance(filteredByActive: Bool = true) -> Single<CoinsBalance> {
        if filteredByActive {
            return getFilteredByActiveCoinsBalance()
        } else {
            return getAllCoinsBalance()
        }
    }
    
  func getFilteredByActiveCoinsBalance() -> Single<CoinsBalance> {
    return walletStorage.get()
      .map { $0.coins.filter { $0.isVisible } }
      .asObservable()
      .withLatestFrom(accountStorage.get()) { ($1, $0) }
      .flatMap { [api] in
        api.getCoinsBalance(userId: $0.userId, coins: $1)
      }
      .doOnNext { [unowned self] in self.updateIndexes(for: $0) }
      .asSingle()
  }
    
    func getAllCoinsBalance() -> Single<CoinsBalance> {
      return walletStorage.get()
        .map { $0.coins }
        .asObservable()
        .withLatestFrom(accountStorage.get()) { ($1, $0) }
        .flatMap { [api] in
          api.getCoinsBalance(userId: $0.userId, coins: $1)
        }
        .doOnNext { [unowned self] in self.updateIndexes(for: $0) }
        .asSingle()
    }
    
  private func updateIndexes(for coinsBalance: CoinsBalance) {
    let typesWithIndexes = coinsBalance.coins.map { ($0.type, $0.index) }
    Observable.from(typesWithIndexes)
      .flatMap { [walletStorage] in walletStorage.changeIndex(of: $0, with: $1) }
      .subscribe()
      .disposed(by: disposeBag)
  }
  
  func getCoinDetails(for type: CustomCoinType) -> Single<CoinDetails> {
    return api.getCoinDetails(type: type)
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
      }.asSingle()
  }
  
  func editTrade(data: P2PEditTradeDataModel) -> Single<Trade> {
    return accountStorage.get()
      .asObservable()
      .flatMap { [api] user in
        api.editTrade(userId: user.userId, data: data)
      }.asSingle()
  }
    
  func cancelTrade(id: String) -> Single<Trade> {
    return accountStorage.get()
      .asObservable()
      .flatMap { [api] user in
        api.cancelTrade(userId: user.userId, id: id)
      }.asSingle()
  }
  
  func getTrades() -> Single<Trades> {
      return accountStorage.get().flatMap{ [api] in api.getTrades(userId: $0.userId)}
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
          }.asSingle()
    }
  
}

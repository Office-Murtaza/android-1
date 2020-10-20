import Foundation
import RxSwift
import TrustWalletCore

protocol WalletUsecase {
  func getCoinsBalance() -> Single<CoinsBalance>
  func getCoinDetails(for type: CustomCoinType) -> Single<CoinDetails>
  func getPriceChartData(for type: CustomCoinType) -> Single<PriceChartData>
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
  
  func getCoinsBalance() -> Single<CoinsBalance> {
    return walletStorage.get()
      .map { $0.coins.filter { $0.isVisible } }
      .asObservable()
      .withLatestFrom(accountStorage.get()) { ($1, $0) }
      .flatMap { [api] in api.getCoinsBalance(userId: $0.userId, coins: $1) }
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
  
  func getPriceChartData(for type: CustomCoinType) -> Single<PriceChartData> {
    return accountStorage.get()
      .flatMap { [api] in api.getPriceChartData(userId: $0.userId, type: type) }
  }
  
}

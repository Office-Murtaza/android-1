import Foundation
import RxSwift

protocol CoinsBalanceUsecase {
  func getCoinsBalance() -> Single<CoinsBalance>
}

class CoinsBalanceUsecaseImpl: CoinsBalanceUsecase {
  
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
    let coinsBalanceObservable = accountStorage.get()
      .flatMap { [api] in api.getCoinsBalance(userId: $0.userId) }
      .asObservable()
    
    let walletObservable = walletStorage.get().asObservable()
    
    return Observable.combineLatest(coinsBalanceObservable, walletObservable)
      .map { (coinsBalance, wallet) -> CoinsBalance in
        var filteredCoinsBalance = coinsBalance
        
        filteredCoinsBalance.coins = filteredCoinsBalance.coins.filter { coinBalance in
          return wallet.coins.contains { coin in
            coin.type == coinBalance.type && coin.isVisible
          }
        }
        
        return filteredCoinsBalance
      }
      .asSingle()
  }
  
}

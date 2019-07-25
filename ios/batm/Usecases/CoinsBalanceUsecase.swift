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
    return walletStorage.get()
      .asObservable()
      .map { $0.coins.filter { $0.isVisible } }
      .withLatestFrom(accountStorage.get()) { ($1, $0) }
      .flatMap { [api] in api.getCoinsBalance(userId: $0.userId, coins: $1) }
      .asSingle()
  }
  
}

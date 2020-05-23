import Foundation
import RxSwift
import TrustWalletCore

protocol TradesUsecase {
  func getBuyTrades(for type: CoinType, from page: Int) -> Single<BuySellTrades>
  func getSellTrades(for type: CoinType, from page: Int) -> Single<BuySellTrades>
}

class TradesUsecaseImpl: TradesUsecase {
  
  let api: APIGateway
  let accountStorage: AccountStorage
  
  init(api: APIGateway,
       accountStorage: AccountStorage) {
    self.api = api
    self.accountStorage = accountStorage
  }
  
  func getBuyTrades(for type: CoinType, from page: Int) -> Single<BuySellTrades> {
    return accountStorage.get()
      .flatMap { [api] in api.getBuyTrades(userId: $0.userId, type: type, page: page) }
  }
  
  func getSellTrades(for type: CoinType, from page: Int) -> Single<BuySellTrades> {
    return accountStorage.get()
      .flatMap { [api] in api.getSellTrades(userId: $0.userId, type: type, page: page) }
  }
  
}

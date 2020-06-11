import Foundation
import RxSwift
import TrustWalletCore

protocol TradesUsecase {
  func getBuyTrades(for type: CoinType, from page: Int) -> Single<BuySellTrades>
  func getSellTrades(for type: CoinType, from page: Int) -> Single<BuySellTrades>
  func submitTradeRequest(for data: SubmitTradeRequestData) -> Completable
  func submitTrade(for data: SubmitTradeData) -> Completable
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
  
  func submitTradeRequest(for data: SubmitTradeRequestData) -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [api] in api.submitTradeRequest(userId: $0.userId, data: data) }
  }
  
  func submitTrade(for data: SubmitTradeData) -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [api] in api.submitTrade(userId: $0.userId, data: data) }
  }
  
}

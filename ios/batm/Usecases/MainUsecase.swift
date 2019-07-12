import Foundation
import RxSwift

protocol CoinsBalanceUsecase {
  func getCoinsBalance() -> Single<CoinsBalance>
}

class CoinsBalanceUsecaseImpl: CoinsBalanceUsecase {
  
  let api: APIGateway
  let accountStorage: AccountStorage
  
  init(api: APIGateway,
       accountStorage: AccountStorage) {
    self.api = api
    self.accountStorage = accountStorage
  }
  
  func getCoinsBalance() -> Single<CoinsBalance> {
    return accountStorage.get()
      .flatMap { [api] in api.getCoinsBalance(userId: $0.userId) }
  }
  
}

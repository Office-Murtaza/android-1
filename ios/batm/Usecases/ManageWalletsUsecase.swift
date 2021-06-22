import Foundation
import RxSwift
import RxCocoa

protocol ManageWalletsUsecase {
  func getCoins() -> Single<[BTMCoin]>
  func changeVisibility(of coin: BTMCoin) -> Single<Bool>
}

class ManageWalletsUsecaseImpl: ManageWalletsUsecase {
  let walletStorage: BTMWalletStorage
  let api: APIGateway
  let accountStorage: AccountStorage
  
  init(walletStorage: BTMWalletStorage,
       api: APIGateway,
       accountStorage: AccountStorage) {
    self.walletStorage = walletStorage
    self.api = api
    self.accountStorage = accountStorage
  }
  
  func getCoins() -> Single<[BTMCoin]> {
    return walletStorage.get().map { $0.coins }
  }
  
  func changeVisibility(of coin: BTMCoin) -> Single<Bool> {
    return  walletStorage.changeVisibility(of: coin).andThen(changeVisibilityRequest(coin: coin))
  }
  
  func changeVisibilityRequest(coin: BTMCoin) -> Single<Bool> {
    return accountStorage.get()
      .flatMap{ [api] account in api.manageCoins(userId: account.userId, coin: coin.type.code, visible: !coin.isVisible)}
  }
}

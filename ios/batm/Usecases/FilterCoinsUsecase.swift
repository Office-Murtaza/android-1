import Foundation
import RxSwift

protocol FilterCoinsUsecase {
  func getCoins() -> Single<[BTMCoin]>
  func changeVisibility(of coin: BTMCoin) -> Completable
}

class FilterCoinsUsecaseImpl: FilterCoinsUsecase {
  
  let walletStorage: BTMWalletStorage
  
  init(walletStorage: BTMWalletStorage) {
    self.walletStorage = walletStorage
  }
  
  func getCoins() -> Single<[BTMCoin]> {
    return walletStorage.get().map { $0.coins }
  }
  
  func changeVisibility(of coin: BTMCoin) -> Completable {
    return walletStorage.changeVisibility(of: coin)
  }
}

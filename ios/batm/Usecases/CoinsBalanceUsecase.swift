import Foundation
import RxSwift

protocol CoinsBalanceUsecase {
  func getCoinsBalance() -> Single<CoinsBalance>
}

class CoinsBalanceUsecaseImpl: CoinsBalanceUsecase, HasDisposeBag {
  
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
    return accountStorage.get()
      .flatMap { [api] in api.getCoinsFee(userId: $0.userId) }
      .asObservable()
      .doOnNext { [unowned self] in self.updateFees(for: $0) }
      .flatMap { [walletStorage] _ in walletStorage.get() }
      .map { $0.coins.filter { $0.isVisible } }
      .withLatestFrom(accountStorage.get()) { ($1, $0) }
      .flatMap { [api] in api.getCoinsBalance(userId: $0.userId, coins: $1) }
      .doOnNext { [unowned self] in self.updateIndexes(for: $0) }
      .asSingle()
  }
  
  private func updateFees(for coinsFee: CoinsFee) {
    Observable.from(coinsFee.fees)
      .flatMap { [walletStorage] coinFee -> Completable in
        if let fee = coinFee.fee {
          return walletStorage.changeFee(of: coinFee.type, with: fee)
        }
        
        if let gasPrice = coinFee.gasPrice, let gasLimit = coinFee.gasLimit {
          return walletStorage.changeGas(of: coinFee.type, price: gasPrice, limit: gasLimit)
        }
        
        return .empty()
      }
      .subscribe()
      .disposed(by: disposeBag)
  }
  
  private func updateIndexes(for coinsBalance: CoinsBalance) {
    let typesWithIndexes = coinsBalance.coins.map { ($0.type, $0.index) }
    Observable.from(typesWithIndexes)
      .flatMap { [walletStorage] in walletStorage.changeIndex(of: $0, with: $1) }
      .subscribe()
      .disposed(by: disposeBag)
  }
  
}

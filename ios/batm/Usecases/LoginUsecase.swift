import Foundation
import RxSwift

protocol LoginUsecase {
  func createAccount(phoneNumber: String, password: String) -> Completable
  func verifyCode(code: String) -> Completable
  func getSeedPhrase() -> Single<String>
}

class LoginUsecaseImpl: LoginUsecase {
  
  let api: APIGateway
  let accountStorage: AccountStorage
  let walletStorage: BTMWalletStorage
  let walletService: WalletService
  
  init(api: APIGateway,
       accountStorage: AccountStorage,
       walletStorage: BTMWalletStorage,
       walletService: WalletService) {
    self.api = api
    self.accountStorage = accountStorage
    self.walletStorage = walletStorage
    self.walletService = walletService
  }
  
  func createAccount(phoneNumber: String, password: String) -> Completable {
    return api.createAccount(phoneNumber: phoneNumber, password: password)
      .asObservable()
      .flatMap { [accountStorage] in accountStorage.save(account: $0).andThen(Observable.just($0)) }
      .toCompletable()
  }
  
  func verifyCode(code: String) -> Completable {
    return accountStorage.get()
      .asObservable()
      .flatMap { [api] in api.verifyCode(userId: $0.userId, code: code).andThen(Observable.just(())) }
      .flatMap { [walletService] in walletService.createWallet().andThen(Observable.just(())) }
      .flatMap { [unowned self] _ in self.addCoins() }
      .toCompletable()
  }
  
  func getSeedPhrase() -> Single<String> {
    return walletStorage.get()
      .map { $0.seedPhrase }
  }
  
  private func addCoins() -> Completable {
    return Observable.combineLatest(accountStorage.get().asObservable(),
                                    walletStorage.get().asObservable())
      .flatMap { [api] in api.addCoins(userId: $0.userId, coins: $1.coinAddresses).andThen(Observable.just(())) }
      .toCompletable()
  }
  
}

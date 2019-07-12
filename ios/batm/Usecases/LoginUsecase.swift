import Foundation
import RxSwift

enum LoginState {
  case loggedOut
  case loggedIn
}

protocol LoginUsecase {
  func getLogoutObservable() -> Observable<Void>
  func getLoginState() -> Single<LoginState>
  func createAccount(phoneNumber: String, password: String) -> Completable
  func recoverWallet(phoneNumber: String, password: String) -> Completable
  func verifyCode(code: String) -> Completable
  func createWallet() -> Completable
  func recoverWallet(seedPhrase: String) -> Completable
  func addCoins() -> Completable
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
  
  func getLogoutObservable() -> Observable<Void> {
    return accountStorage.stateObservable
      .distinctUntilChanged()
      .filter { $0 == nil }
      .toVoid()
  }
  
  func getLoginState() -> Single<LoginState> {
    return accountStorage.get()
      .map { _ in return .loggedIn }
      .catchError { _ in return .just(.loggedOut) }
  }
  
  func createAccount(phoneNumber: String, password: String) -> Completable {
    return api.createAccount(phoneNumber: phoneNumber, password: password)
      .asObservable()
      .flatMap { [accountStorage] in accountStorage.save(account: $0).andThen(Observable.just($0)) }
      .toCompletable()
  }
  
  func recoverWallet(phoneNumber: String, password: String) -> Completable {
    return api.recoverWallet(phoneNumber: phoneNumber, password: password)
      .asObservable()
      .flatMap { [accountStorage] in accountStorage.save(account: $0).andThen(Observable.just($0)) }
      .toCompletable()
  }
  
  func verifyCode(code: String) -> Completable {
    return accountStorage.get()
      .asObservable()
      .flatMap { [api] in api.verifyCode(userId: $0.userId, code: code).andThen(Observable.just(())) }
      .toCompletable()
  }
  
  func createWallet() -> Completable {
    return walletService.createWallet()
  }
  
  func recoverWallet(seedPhrase: String) -> Completable {
    return walletService.recoverWallet(seedPhrase: seedPhrase)
  }
  
  func addCoins() -> Completable {
    return Observable.combineLatest(accountStorage.get().asObservable(),
                                    walletStorage.get().asObservable())
      .flatMap { [api] in api.addCoins(userId: $0.userId, coins: $1.coinAddresses).andThen(Observable.just(())) }
      .toCompletable()
  }
  
  func getSeedPhrase() -> Single<String> {
    return walletStorage.get()
      .map { $0.seedPhrase }
  }
  
}

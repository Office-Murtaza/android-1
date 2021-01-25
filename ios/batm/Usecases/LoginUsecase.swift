import Foundation
import RxSwift

enum LoginState {
  case loggedOut
  case setupPinCode
  case loggedIn
}

protocol LoginUsecase {
  func getLogoutObservable() -> Observable<Void>
  func getLoginState() -> Single<LoginState>
  func checkCreatingAccount(phoneNumber: String, password: String) -> Completable
  func checkRecoveringAccount(phoneNumber: String, password: String) -> Completable
    func createWallet(seedPhrase: String) -> Completable
  func getSeedPhrase() -> Single<String>
  func createAccount(phoneNumber: String, password: String) -> Completable
  func recoverWallet(phoneNumber: String, password: String, seedPhrase: String) -> Completable
}

class LoginUsecaseImpl: LoginUsecase {
  
  let api: APIGateway
  let accountStorage: AccountStorage
  let walletStorage: BTMWalletStorage
  let pinCodeStorage: PinCodeStorage
  let walletService: WalletService
  
  init(api: APIGateway,
       accountStorage: AccountStorage,
       walletStorage: BTMWalletStorage,
       pinCodeStorage: PinCodeStorage,
       walletService: WalletService) {
    self.api = api
    self.accountStorage = accountStorage
    self.walletStorage = walletStorage
    self.pinCodeStorage = pinCodeStorage
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
      .flatMap { [walletStorage] _ in walletStorage.get() }
      .flatMap { [pinCodeStorage] _ in pinCodeStorage.get() }
      .map { _ in return .loggedIn }
      .catchError { error in
        if let accountError = error as? AccountStorageError, accountError == .notFound,
          let walletError = error as? BTMWalletStorageError, walletError == .notFound {
          return .just(.loggedOut)
        }
        
        if let error = error as? PinCodeStorageError, error == .notFound {
          return .just(.setupPinCode)
        }
        
        return .just(.loggedOut)
      }
  }
  
  func checkCreatingAccount(phoneNumber: String, password: String) -> Completable {
    return api.checkAccount(phoneNumber: phoneNumber, password: password)
      .flatMapCompletable {
        if $0.phoneExist {
          let error = ServerError(code: 2, message: localize(L.CreateWallet.Form.Error.existedPhoneNumber))
          return .error(APIError.serverError(error))
        }
        
        return .empty()
      }
  }
  
  func checkRecoveringAccount(phoneNumber: String, password: String) -> Completable {
    return api.checkAccount(phoneNumber: phoneNumber, password: password)
      .flatMapCompletable {
        if !$0.phoneExist {
          let error = ServerError(code: 2, message: localize(L.Recover.Form.Error.notExistedPhoneNumber))
          return .error(APIError.serverError(error))
        }
        
        if !$0.passwordMatch {
          let error = ServerError(code: 3, message: localize(L.Recover.Form.Error.notMatchPassword))
          return .error(APIError.serverError(error))
        }
        
        return .empty()
      }
  }
  
    func createWallet(seedPhrase: String) -> Completable {
    return walletService.createWallet(seedPhrase: seedPhrase)
  }
  
  func getSeedPhrase() -> Single<String> {
    return walletStorage.get()
      .map { $0.seedPhrase }
  }
  
  func createAccount(phoneNumber: String, password: String) -> Completable {
    return walletStorage.get()
      .map { $0.coins.map { CoinAddress(coin: $0) } }
      .flatMap { [api] in api.createAccount(phoneNumber: phoneNumber, password: password, coinAddresses: $0) }
      .flatMapCompletable { [accountStorage, walletStorage] in
        let changeIndexMapper = $0.coins.map { coin in walletStorage.changeIndex(of: coin.type, with: coin.index) }
        let mapperConcat = Completable.concat(changeIndexMapper)
        return accountStorage.save(account: $0.account).andThen(mapperConcat)
      }
  }
  
  func recoverWallet(phoneNumber: String, password: String, seedPhrase: String) -> Completable {
    return walletService.recoverWallet(seedPhrase: seedPhrase)
      .andThen(walletStorage.get())
      .map { $0.coins.map { CoinAddress(coin: $0) } }
      .flatMap { [api] in api.recoverWallet(phoneNumber: phoneNumber, password: password, coinAddresses: $0) }
      .flatMapCompletable { [accountStorage, walletStorage] in
        let changeIndexMapper = $0.coins.map { coin in walletStorage.changeIndex(of: coin.type, with: coin.index) }
        let mapperConcat = Completable.concat(changeIndexMapper)
        return accountStorage.save(account: $0.account).andThen(mapperConcat)
      }
  }
}

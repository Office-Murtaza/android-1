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
  func checkAndVerifyCreatingAccount(phoneNumber: String, password: String) -> Single<PhoneVerificationResponse>
  func checkAndVerifyRecoveringAccount(phoneNumber: String, password: String) -> Single<PhoneVerificationResponse>
  func createWallet() -> Completable
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
  
  func checkAndVerifyCreatingAccount(phoneNumber: String, password: String) -> Single<PhoneVerificationResponse> {
    return api.checkAccount(phoneNumber: phoneNumber, password: password)
      .flatMap { [api] in
        if $0.phoneExist {
          return .error(APIError.serverError(localize(L.CreateWallet.Form.Error.existedPhoneNumber)))
        }
        
        return api.verifyPhone(phoneNumber: phoneNumber)
      }
  }
  
  func checkAndVerifyRecoveringAccount(phoneNumber: String, password: String) -> Single<PhoneVerificationResponse> {
    return api.checkAccount(phoneNumber: phoneNumber, password: password)
      .flatMap { [api] in
        if !$0.phoneExist {
          return .error(APIError.serverError(localize(L.Recover.Form.Error.notExistedPhoneNumber)))
        }
        
        if !$0.passwordMatch {
          return .error(APIError.serverError(localize(L.Recover.Form.Error.notMatchPassword)))
        }
        
        return api.verifyPhone(phoneNumber: phoneNumber)
      }
  }
  
  func createWallet() -> Completable {
    return walletService.createWallet()
  }
  
  func getSeedPhrase() -> Single<String> {
    return walletStorage.get()
      .map { $0.seedPhrase }
  }
  
  func createAccount(phoneNumber: String, password: String) -> Completable {
    return walletStorage.get()
      .map { $0.coins.map { CoinAddress(coin: $0) } }
      .flatMap { [api] in api.createAccount(phoneNumber: phoneNumber, password: password, coinAddresses: $0) }
      .asObservable()
      .flatMap { [accountStorage] in accountStorage.save(account: $0).andThen(Observable.just($0)) }
      .toCompletable()
  }
  
  func recoverWallet(phoneNumber: String, password: String, seedPhrase: String) -> Completable {
    return walletService.recoverWallet(seedPhrase: seedPhrase)
      .andThen(walletStorage.get())
      .map { $0.coins.map { CoinAddress(coin: $0) } }
      .flatMap { [api] in api.recoverWallet(phoneNumber: phoneNumber, password: password, coinAddresses: $0) }
      .asObservable()
      .flatMap { [accountStorage] in accountStorage.save(account: $0).andThen(Observable.just($0)) }
      .toCompletable()
  }
  
}

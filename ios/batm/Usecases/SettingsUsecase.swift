import Foundation
import RxSwift

protocol SettingsUsecase {
  func getPhoneNumber() -> Single<PhoneNumber>
  func checkPassword(password: String) -> Single<Bool>
  func changePhone(phoneNumber: String) -> Completable
  func confirmPhone(phoneNumber: String, code: String) -> Completable
  func changePassword(oldPassword: String, newPassword: String) -> Completable
  func changePin(oldPin: String, newPin: String) -> Completable
  func unlink() -> Completable
  func getVerificationInfo() -> Single<VerificationInfo>
  func sendVerification(userData: VerificationUserData) -> Completable
  func sendVIPVerification(userData: VIPVerificationUserData) -> Completable
}

class SettingsUsecaseImpl: SettingsUsecase {
  
  let api: APIGateway
  let accountStorage: AccountStorage
  let walletStorage: BTMWalletStorage
  let refreshCredentialsService: RefreshCredentialsService
  let logoutUsecase: LogoutUsecase
  let pinCodeUsecase: PinCodeUsecase
  
  init(api: APIGateway,
       accountStorage: AccountStorage,
       walletStorage: BTMWalletStorage,
       refreshCredentialsService: RefreshCredentialsService,
       logoutUsecase: LogoutUsecase,
       pinCodeUsecase: PinCodeUsecase) {
    self.api = api
    self.accountStorage = accountStorage
    self.walletStorage = walletStorage
    self.refreshCredentialsService = refreshCredentialsService
    self.logoutUsecase = logoutUsecase
    self.pinCodeUsecase = pinCodeUsecase
  }
  
  func getPhoneNumber() -> Single<PhoneNumber> {
    return accountStorage.get()
      .flatMap { [api] in api.getPhoneNumber(userId: $0.userId) }
  }
  
  func checkPassword(password: String) -> Single<Bool> {
    return accountStorage.get()
      .flatMap { [api] in api.checkPassword(userId: $0.userId, password: password) }
  }
  
  func changePhone(phoneNumber: String) -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [api] in api.changePhone(userId: $0.userId,
                                                     phoneNumber: phoneNumber) }
  }
  
  func confirmPhone(phoneNumber: String, code: String) -> Completable {
    return accountStorage.get()
      .flatMap { [api] in
        return api.confirmPhone(userId: $0.userId, phoneNumber: phoneNumber, code: code)
          .andThen(Single.just($0))
      }
      .flatMapCompletable { [refreshCredentialsService] _ in refreshCredentialsService.refresh() }
  }
  
  func changePassword(oldPassword: String, newPassword: String) -> Completable {
    return accountStorage.get()
      .flatMap { [api] in
        return api.changePassword(userId: $0.userId, oldPassword: oldPassword, newPassword: newPassword)
          .andThen(Single.just($0))
      }
      .flatMapCompletable { [refreshCredentialsService] _ in refreshCredentialsService.refresh() }
  }
  
  func changePin(oldPin: String, newPin: String) -> Completable {
    return pinCodeUsecase.verify(pinCode: oldPin)
      .andThen(pinCodeUsecase.save(pinCode: newPin))
  }
  
  func unlink() -> Completable {
    return accountStorage.get()
      .flatMap { [api] in api.unlink(userId: $0.userId).andThen(Single.just($0)) }
      .flatMap { [logoutUsecase] _ in logoutUsecase.logout() }
      .asCompletable()
  }
  
  func getVerificationInfo() -> Single<VerificationInfo> {
    return accountStorage.get()
      .flatMap { [api] in api.getVerificationInfo(userId: $0.userId) }
  }
  
  func sendVerification(userData: VerificationUserData) -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [api] in api.sendVerification(userId: $0.userId, userData: userData) }
  }
  
  func sendVIPVerification(userData: VIPVerificationUserData) -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [api] in api.sendVIPVerification(userId: $0.userId, userData: userData) }
  }
  
}

import Foundation
import RxSwift

protocol SettingsUsecase {
  func getPhoneNumber() -> Single<PhoneNumber>
  func verifyAccount(phoneNumber: String) -> Single<PhoneVerificationResponse>
  func verifyPassword(password: String) -> Single<Bool>
  func verifyPhone(phoneNumber: String) -> Single<Bool>
  func updatePhone(phoneNumber: String) -> Completable
  func confirmPhone(phoneNumber: String, code: String) -> Completable
  func updatePassword(oldPassword: String, newPassword: String) -> Completable
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
  
  func verifyPassword(password: String) -> Single<Bool> {
    return accountStorage.get()
      .flatMap { [api] in api.verifyPassword(userId: $0.userId, password: password) }
  }
  
  func verifyPhone(phoneNumber: String) -> Single<Bool> {
    return accountStorage.get()
      .flatMap { [api] in api.verifyPhone(userId: $0.userId, phoneNumber: phoneNumber) }
  }
  
  func verifyAccount(phoneNumber: String) -> Single<PhoneVerificationResponse> {
    return api.verifyPhone(phoneNumber: phoneNumber)
  }
  
  func updatePhone(phoneNumber: String) -> Completable {
    return accountStorage.get()
      .flatMapCompletable { [api] in api.updatePhone(userId: $0.userId,
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
  
  func updatePassword(oldPassword: String, newPassword: String) -> Completable {
    return accountStorage.get()
      .flatMap { [api] in
        return api.updatePassword(userId: $0.userId, oldPassword: oldPassword, newPassword: newPassword)
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

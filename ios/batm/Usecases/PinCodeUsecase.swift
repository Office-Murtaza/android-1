import Foundation
import RxSwift

enum PinCodeError: Error {
  case notMatch
}

protocol PinCodeUsecase {
  func get() -> Single<String>
  func save(pinCode: String) -> Completable
  func verify(pinCode: String) -> Completable
  func refresh() -> Completable
}

class PinCodeUsecaseImpl: PinCodeUsecase {
  
  let pinCodeStorage: PinCodeStorage
  let refreshService: RefreshCredentialsService
  
  init(pinCodeStorage: PinCodeStorage,
       refreshService: RefreshCredentialsService) {
    self.pinCodeStorage = pinCodeStorage
    self.refreshService = refreshService
  }
  
  func get() -> Single<String> {
    return pinCodeStorage.get()
  }
  
  func save(pinCode: String) -> Completable {
    return pinCodeStorage.save(pinCode: pinCode)
  }
  
  func verify(pinCode: String) -> Completable {
    return pinCodeStorage.get()
      .map { savedPin -> Void in
        if savedPin != pinCode {
          throw PinCodeError.notMatch
        }
      }
      .toCompletable()
  }
  
  func refresh() -> Completable {
    return refreshService.refresh()
  }
  
}

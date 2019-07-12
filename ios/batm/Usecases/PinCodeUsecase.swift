import Foundation
import RxSwift

protocol PinCodeUsecase {
  func save(pinCode: String) -> Completable
  func verify(pinCode: String) -> Single<Bool>
}

class PinCodeUsecaseImpl: PinCodeUsecase {
  
  let pinCodeStorage: PinCodeStorage
  
  init(pinCodeStorage: PinCodeStorage) {
    self.pinCodeStorage = pinCodeStorage
  }
  
  func save(pinCode: String) -> Completable {
    return pinCodeStorage.save(pinCode: pinCode)
  }
  
  func verify(pinCode: String) -> Single<Bool> {
    return pinCodeStorage.get()
      .map { $0 == pinCode }
  }
  
}

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
    func startSocketService()
}

class PinCodeUsecaseImpl: PinCodeUsecase {
    let pinCodeStorage: PinCodeStorage
    let refreshService: RefreshCredentialsService
    let mainSocketService: MainSocketService
  
    init(pinCodeStorage: PinCodeStorage,
         refreshService: RefreshCredentialsService,
         mainSocketService: MainSocketService) {
        self.pinCodeStorage = pinCodeStorage
        self.refreshService = refreshService
        self.mainSocketService = mainSocketService
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
  
    func startSocketService() {
        mainSocketService.start()
    }
}

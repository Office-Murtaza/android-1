import Foundation
import RxSwift
import RxCocoa

enum LogoutState: Equatable {
  case loggedOut
  case errored(StorageError)
}

protocol LogoutUsecase {
  func logout() -> Single<LogoutState>
}

class LogoutUsecaseImpl: LogoutUsecase {
  
  private let storageRegistry: LogoutStorageRegistry
  private let networkService: NetworkRequestExecutor
  let accountStorage: AccountStorage
  
  init(storageRegistry: LogoutStorageRegistry,
       networkService: NetworkRequestExecutor,
       accountStorage: AccountStorage) {
    self.storageRegistry = storageRegistry
    self.networkService = networkService
    self.accountStorage = accountStorage
  }
  
  func logout() -> Single<LogoutState> {
    return Single.create { [storageRegistry] observer in
      return storageRegistry.clear()
        .subscribe(onCompleted: { observer(.success(.loggedOut)) },
                   onError: {
                    let error: StorageError = castable($0)
                      .map(as: StorageError.self) { $0 }
                      .extract(StorageError.internalError($0.localizedDescription))
                    observer(.success(.errored(error)))
                   })
    }
  }
}

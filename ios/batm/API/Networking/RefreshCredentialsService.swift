import Moya
import RxSwift
import RxCocoa

protocol RefreshCredentialsService: class {
  func refresh() -> Completable
}

class RefreshCredentialsServiceImpl: RefreshCredentialsService {
  
  private let credentialsRelay = PublishRelay<(Account?, Error?)>()
  private var isFetching = false
  
  let networkService: NetworkRequestExecutor
  let accountStorage: AccountStorage
  
  init(networkService: NetworkRequestExecutor,
       accountStorage: AccountStorage) {
    self.networkService = networkService
    self.accountStorage = accountStorage
  }
  
  func refresh() -> Completable {
    if isFetching {
      return credentialsRelay
        .take(1)
        .asSingle()
        .map { (account, error) -> Account in
          if let account = account {
            return account
          }
          
          if let error = error {
            throw error
          }
          
          throw APIError.unknown
        }
        .asCompletable()
    }
    
    isFetching = true
    
    return accountStorage.get()
      .flatMap { [networkService] account -> Single<APIResponse<Account>> in
        let request = RefreshTokenRequest(account: account)
        return networkService.execute(request)
      }
      .flatMap {
        switch $0 {
        case let .response(response):
          return Single.just(response)
        case let .error(error):
          return Single.error(error)
        }
      }
      .flatMap { [accountStorage] in accountStorage.save(account: $0).andThen(.just($0)) }
      .do(onSuccess: { [unowned self] in
        self.isFetching = false
        self.credentialsRelay.accept(($0, nil))
      }, onError: { [unowned self] in
        self.isFetching = false
        self.credentialsRelay.accept((nil, $0))
      })
      .asCompletable()
  }
}

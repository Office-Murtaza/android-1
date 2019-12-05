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
  let logoutUsecase: LogoutUsecase
  
  init(networkService: NetworkRequestExecutor,
       accountStorage: AccountStorage,
       logoutUsecase: LogoutUsecase) {
    self.networkService = networkService
    self.accountStorage = accountStorage
    self.logoutUsecase = logoutUsecase
  }
  
  func refresh() -> Completable {
    Completable.deferred {
      if self.isFetching {
        return self.credentialsRelay
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
      
      self.isFetching = true
      
      return self.accountStorage.get()
        .flatMap { [unowned self] account -> Single<APIResponse<Account>> in
          let request = RefreshTokenRequest(account: account)
          return self.networkService.execute(request)
        }
        .flatMap {
          switch $0 {
          case let .response(response):
            return Single.just(response)
          case let .error(error):
            return Single.error(error)
          }
        }
        .flatMap { [unowned self] in self.accountStorage.save(account: $0).andThen(.just($0)) }
        .do(onSuccess: { [unowned self] in
          self.isFetching = false
          self.credentialsRelay.accept(($0, nil))
          }, onError: { [unowned self] in
            self.isFetching = false
            self.credentialsRelay.accept((nil, $0))
        })
        .asCompletable()
        .catchError { [unowned self] in
          let mappedError = $0.mapToAPIError()
          let completableError = Completable.error(mappedError)
          
          if mappedError == .notAuthorized {
            return self.logoutUsecase.logout().flatMapCompletable { _ in completableError }
          }
          
          return completableError
        }
    }
  }
}

import Moya
import RxSwift
import ObjectMapper

final class BTMNetworkService: NetworkRequestExecutor {
  
  let network: NetworkService
  let credentials: AccountStorage
  
  init(networkService: NetworkService,
       credentials: AccountStorage) {
    self.network = networkService
    self.credentials = credentials
  }
  
  // MARK: - NetworkRequestExecutor
  
  func execute<Request, Response>(_ apiRequest: Request) -> Single<Response>
    where Request: APIRequest, Response == Request.ResponseType, Request.ResponseTrait == SingleResponseTrait {
      return run(apiRequest) { [network] in network.execute($0, headers: $1) }
        .asSingle()
  }
  
  func execute<Request, Response>(_ apiRequest: Request) -> Single<[Response]>
    where Request: APIRequest, Response == Request.ResponseType, Request.ResponseTrait == CollectionResponseTrait {
      return run(apiRequest) { [network] in network.execute($0, headers: $1) }
        .asSingle()
  }
  
  func execute<Request, Response>(_ apiRequest: Request) -> ObservableProgress<Response>
    where Request: APIRequest, Response == Request.ResponseType,
    Request.ResponseTrait == SingleResponseTrait & ProgressResponseTrait {
      return run(apiRequest) { [network] in network.execute($0, headers: $1) }
  }
  
  func execute<Request>(_ apiRequest: Request) -> Completable
    where Request: SimpleRequest, Request.ResponseTrait == NopResponseTrait {
      return run(apiRequest) { [network] in network.execute($0, headers: $1)}
        .toCompletable()
  }
  
  // MARK: - Private
  
  private func run<O, Request>(_ request: Request,
                               execute: @escaping (Request, [String: String]?) -> O) -> Observable<O.E>
    where O: ObservableConvertibleType, Request: SimpleRequest {
      let requestSignal = headers(for: request)
        .asObservable()
        .flatMap { execute(request, $0) }
        .catchError { [unowned self] in throw self.map(error: $0) }
      return retry(request, signal: requestSignal)
  }
  
  private func retry<Request, O>(_ request: Request, signal: O) -> Observable<O.E>
    where Request: SimpleRequest, O: ObservableConvertibleType {
      if request is Retriable {
        return signal
          .asObservable()
          .retryWhen { [unowned self] in self.retryNotAuthorized(errors: $0) }
      } else {
        return signal
          .asObservable()
      }
  }
  
  private func retryNotAuthorized(errors: Observable<Error>) -> Observable<Void> {
    return errors.take(1)
      .map { [unowned self] in self.map(error: $0) }
      .flatMap { [unowned self] error -> Observable<Void> in
        if error == .notAuthorized {
          return self.refreshCredentials()
            .andThen(.just(()))
        } else {
          return .error(error)
        }
      }
      .toVoid()
  }
  
  private func refreshCredentials() -> Completable {
    return credentials.get()
      .asObservable()
      .flatMap { [unowned self] creds -> Single<Account> in
        return self.refreshCredentials(creds)
      }
      .toCompletable()
  }
  
  private func refreshCredentials(_ creds: Account) -> Single<Account> {
    return .just(creds)
//    return refreshService.refresh(credentials: creds)
//      .catchError { [unowned self, logoutUsecase] in
//        let mappedError = self.map(error: $0)
//        let singleError = Single<Account>.error(mappedError)
//
//        if mappedError == .notAuthorized {
//          return logoutUsecase.logout().flatMap { _ in singleError }
//        }
//
//        return singleError
//      }
//      .flatMap { [credentials] in credentials.save($0).andThen(.just($0)) }
  }
  
  func headers<Request: SimpleRequest>(for request: Request) -> Single<[String: String]?> {
    if request is AuthorizedRequest {
      return credentials.get()
        .map { ["Authorization": "Bearer " + $0.accessToken] }
        .catchError { _ in .just(nil) }
    }
    return .just(nil)
  }
  
  func map(error: Error) -> APIError {
    return castable(error)
      .map { $0 as APIError }
      .map(convert(error:))
      .extract(.unknown)
  }
  
  func convert(error: MoyaError) -> APIError {
    switch error {
    case let .statusCode(response) where response.statusCode == 422:
      return .notValid
    case let .statusCode(response) where response.statusCode == 409:
      return .conflict
    case let .statusCode(response) where response.statusCode == 401:
      return .notAuthorized
    case let .statusCode(response) where response.statusCode == 404:
      return .notFound
    case .underlying(_, .none):
      return .networkError
    default:
      return .unknown
    }
  }
}

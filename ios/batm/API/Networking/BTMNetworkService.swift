import Moya
import RxSwift
import ObjectMapper

final class BTMNetworkService: NetworkRequestExecutor {
  
  let network: NetworkService
  let accountStorage: AccountStorage
  let logoutUsecase: LogoutUsecase
  let pinCodeService: PinCodeService
  let refreshCredentialsService: RefreshCredentialsService
  let errorService: ErrorService
  
  init(networkService: NetworkService,
       accountStorage: AccountStorage,
       logoutUsecase: LogoutUsecase,
       pinCodeService: PinCodeService,
       refreshCredentialsService: RefreshCredentialsService,
       errorService: ErrorService) {
    self.network = networkService
    self.accountStorage = accountStorage
    self.logoutUsecase = logoutUsecase
    self.pinCodeService = pinCodeService
    self.refreshCredentialsService = refreshCredentialsService
    self.errorService = errorService
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
      
      return retry(request, signal: requestSignal)
        .catchError { [unowned self] error in
          let mappedError = error.mapToAPIError()
          return self.errorService.showError(for: .somethingWentWrong).andThen(.error(mappedError))
        }
  }
  
  private func retry<Request, O>(_ request: Request, signal: O) -> Observable<O.E>
    where Request: SimpleRequest, O: ObservableConvertibleType {
      if request is Retriable {
        return signal
          .asObservable()
          .retryWhen { [unowned self] in self.retryNoConnection(errors: $0) }
          .retryWhen { [unowned self] in self.retryNotAuthorized(errors: $0) }
      }
      
      return signal.asObservable()
  }
  
  private func retryNoConnection(errors: Observable<Error>) -> Observable<Void> {
    return errors
      .map { $0.mapToAPIError() }
      .flatMap { [unowned self] error -> Observable<Void> in
        if error == .noConnection {
          return self.errorService.showError(for: .noConnection).andThen(Observable.just(()).delayed(1))
        }
        
        return .error(error)
    }
    .toVoid()
  }
  
  private func retryNotAuthorized(errors: Observable<Error>) -> Observable<Void> {
    return errors.take(1)
      .map { $0.mapToAPIError() }
      .flatMap { [unowned self] error -> Observable<Void> in
        if error == .notAuthorized {
          return self.refreshCredentials().andThen(.just(()))
        }
        
        return .error(error)
    }
    .toVoid()
  }
  
  private func refreshCredentials() -> Completable {
    return pinCodeService.verifyPinCode()
      .andThen(refreshCredentialsService.refresh())
  }
  
  func headers<Request: SimpleRequest>(for request: Request) -> Single<[String: String]?> {
    if request is AuthorizedRequest {
      return accountStorage.get()
        .map { ["Authorization": "Bearer " + $0.accessToken] }
        .catchError { _ in .just(nil) }
    }
    return .just(nil)
  }
}

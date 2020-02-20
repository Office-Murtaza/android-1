import Moya
import RxSwift
import ObjectMapper
import Reachability

protocol NetworkRequestExecutor {
  func execute<Request: APIRequest, Response>(_ apiRequest: Request) -> Single<Response>
  where Request.ResponseType == Response, Request.ResponseTrait == SingleResponseTrait
  
  func execute<Request: APIRequest, Response>(_ apiRequest: Request) -> Single<[Response]>
  where Request.ResponseType == Response, Request.ResponseTrait == CollectionResponseTrait
  
  func execute<Request: APIRequest, Response>(_ apiRequest: Request) -> ObservableProgress<Response>
  where Request.ResponseType == Response, Request.ResponseTrait == SingleResponseTrait & ProgressResponseTrait
  
  func execute<Request: SimpleRequest>(_ apiRequest: Request) -> Completable
  where Request.ResponseTrait == NopResponseTrait
}

final class NetworkService {
  let baseApiUrl: URL
  let provider: MoyaProvider<MultiTarget>
  let reachability: Reachability?
  
  init(baseApiUrl: URL, provider: MoyaProvider<MultiTarget> = .init()) {
    self.baseApiUrl = baseApiUrl
    self.provider = provider
    self.reachability = Reachability()
    try? self.reachability?.startNotifier()
  }
  
  func execute<Request: APIRequest, Response>(_ apiRequest: Request, headers: [String: String]? = nil)
    -> Single<Response> where Request.ResponseType == Response {
      return run(apiRequest, headers: headers)
        .mapObject(Response.self)
  }
  
  func execute<Request: APIRequest, Response>(_ apiRequest: Request, headers: [String: String]? = nil)
    -> Single<[Response]> where Request.ResponseType == Response {
      return run(apiRequest, headers: headers)
        .mapArray(Response.self)
  }
  
  func execute<Request: APIRequest, Response>(_ apiRequest: Request, headers: [String: String]? = nil)
    -> ObservableProgress<Response>
    where Request.ResponseType == Response, Request.ResponseTrait == SingleResponseTrait & ProgressResponseTrait {
      return runWithProgress(apiRequest, headers: headers)
        .mapObject(Response.self)
  }
  
  func execute<Request: SimpleRequest>(_ apiRequest: Request, headers: [String: String]? = nil) -> Completable {
    return run(apiRequest, headers: headers)
      .toCompletable()
  }
  
  // MARK: - Private
  
  private func runWithProgress<Request>(_ request: Request,
                                        headers: [String: String]?) -> Observable<Moya.ProgressResponse>
    where Request: SimpleRequest {
      if let reachability = reachability, reachability.connection == .none {
        return .error(APIError.noConnection)
      }
      
      return provider.rx.requestWithProgress(prepare(request, headers: headers))
        .filterSuccessfulStatusCodes()
  }
  
  private func run<Request: SimpleRequest>(_ request: Request, headers: [String: String]?) -> Single<Moya.Response> {
    if let reachability = reachability, reachability.connection == .none {
      return .error(APIError.noConnection)
    }
    
    return provider.rx.request(prepare(request, headers: headers))
      .filterSuccessfulStatusCodes()
  }
  
  private func prepare<Request: SimpleRequest>(_ request: Request, headers: [String: String]?) -> MultiTarget {
    return MultiTarget(request.asTargetType(with: baseApiUrl, headers: headers))
  }
}

extension NetworkService: NetworkRequestExecutor {
  func execute<Request, Response>(_ apiRequest: Request) -> Single<Response>
    where Request: APIRequest, Response == Request.ResponseType, Request.ResponseTrait == SingleResponseTrait {
      return execute(apiRequest, headers: nil)
  }
  
  func execute<Request, Response>(_ apiRequest: Request) -> Single<[Response]>
    where Request: APIRequest, Response == Request.ResponseType, Request.ResponseTrait == CollectionResponseTrait {
      return execute(apiRequest, headers: nil)
  }
  
  func execute<Request, Response>(_ apiRequest: Request) -> ObservableProgress<Response>
    where Request: APIRequest, Response == Request.ResponseType,
    Request.ResponseTrait == SingleResponseTrait & ProgressResponseTrait {
      return execute(apiRequest, headers: nil)
  }
  
  func execute<Request>(_ apiRequest: Request) -> Completable
    where Request: SimpleRequest, Request.ResponseTrait == NopResponseTrait {
      return execute(apiRequest, headers: nil)
  }
}

extension ObservableType where E == ProgressResponse {
  func filterSuccessfulStatusCodes() -> Observable<E> {
    return map { progress in
      _ = try progress.response?.filterSuccessfulStatusCodes()
      return progress
    }
  }
}

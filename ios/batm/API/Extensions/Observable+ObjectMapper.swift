import RxSwift
import ObjectMapper
import Moya

enum APIProgressResult<T> {
  case progress(Double)
  case result(T)
}

extension APIProgressResult: Equatable where T: Equatable {
  static func == (lhs: APIProgressResult<T>, rhs: APIProgressResult<T>) -> Bool {
    switch (lhs, rhs) {
    case let (.progress(lhs), .progress(rhs)):
      return lhs == rhs
    case let (.result(lhs), .result(rhs)):
      return lhs == rhs
    default:
      return false
    }
  }
}

typealias ObservableProgress<T> = Observable<APIProgressResult<T>>

extension ObservableType where E == ProgressResponse {
  
  func mapObject<T: ImmutableMappable>(_ type: T.Type, context: MapContext? = nil) -> ObservableProgress<T> {
    return flatMap { progressObject -> ObservableProgress<T> in
      guard let response = progressObject.response else {
        return Observable.just(.progress(progressObject.progress))
      }
      return Observable.just(.result(try response.mapObject(type, context: context)))
    }
  }
  
}

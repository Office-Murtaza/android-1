import Foundation
import Moya
import ObjectMapper

typealias HTTPMethod = Moya.Method
typealias HTTPTask = Moya.Task

protocol AuthorizedRequest {}
protocol Retriable {}
protocol APIRequest: ResponseHolder, SimpleRequest {}

typealias RetriableAPIRequest = APIRequest & Retriable
typealias RetriableSimpleRequest = SimpleRequest & Retriable
typealias AuthorizedAPIRequest = AuthorizedRequest & RetriableAPIRequest
typealias AuthorizedSimpleRequest = AuthorizedRequest & RetriableSimpleRequest

protocol ResponseHolder {
  associatedtype ResponseType: ImmutableMappable
}

protocol SingleResponseTrait { }
protocol CollectionResponseTrait { }
protocol ProgressResponseTrait { }
protocol NopResponseTrait { }

protocol SimpleRequest {
  associatedtype ResponseTrait
  
  var path: String { get }
  var method: HTTPMethod { get }
  var task: HTTPTask { get }
  
  func asTargetType(with baseURL: URL) -> TargetType
  func asTargetType(with baseURL: URL, headers: [String: String]?) -> TargetType
}

extension SimpleRequest {
  func asTargetType(with baseURL: URL) -> TargetType {
    return asTargetType(with: baseURL, headers: nil)
  }
  
  func asTargetType(with baseURL: URL, headers: [String: String]?) -> TargetType {
    return HTTPRequest(baseURL: baseURL,
                       path: path,
                       method: method,
                       task: task,
                       headers: headers)
  }
}

struct HTTPRequest: TargetType {
  let baseURL: URL
  let path: String
  let method: Moya.Method
  let task: Moya.Task
  let headers: [String: String]?
  let sampleData = Data()
}

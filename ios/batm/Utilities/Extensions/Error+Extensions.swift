import Moya
import ObjectMapper

extension Error {
  
  func mapToAPIError() -> APIError {
    return castable(self)
      .map { APIError.serverError($0) }
      .map { $0 as APIError }
      .map(MoyaError.convert(error:))
      .extract(.unknown)
  }
  
  static func convert(error: MoyaError) -> APIError {
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

extension MoyaError {
  
  static func convert(error: MoyaError) -> APIError {
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

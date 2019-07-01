import ObjectMapper

enum APIResponse<T: ImmutableMappable> {
  case response(T)
  case error(APIError)
}

enum APIEmptyResponse {
  case response
  case error(APIError)
}

extension APIResponse: ImmutableMappable {
  init(map: Map) throws {
    if let error: String = try? map.value("error.errorMsg") {
      self = .error(.serverError(error))
      return
    }
    
    guard let responseJSON = map.JSON["response"] as? [String: Any] else {
      self = .error(.unknown)
      return
    }
    
    self = .response(try T(JSON: responseJSON))
  }
}

extension APIEmptyResponse: ImmutableMappable {
  init(map: Map) throws {
    if let error: String = try? map.value("error.errorMsg") {
      self = .error(.serverError(error))
      return
    }
    
    self = .response
  }
}

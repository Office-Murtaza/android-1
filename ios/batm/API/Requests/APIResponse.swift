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
    if let error: ServerError = try? map.value("error") {
      self = .error(.serverError(error))
      return
    }
    
    self = .response(try map.value("response"))
  }
}

extension APIEmptyResponse: ImmutableMappable {
  init(map: Map) throws {
    if let error: ServerError = try? map.value("error") {
      self = .error(.serverError(error))
      return
    }
    
    self = .response
  }
}

import Moya

struct CreateAccountRequest: APIRequest {
  typealias ResponseType = APIResponse<Account>
  typealias ResponseTrait = SingleResponseTrait
  
  let phoneNumber: String
  let password: String
  
  var path: String { return "/user/register" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phoneNumber,
                                           "password": password],
                              encoding: JSONEncoding.default)
  }
}

struct VerifyCodeRequest: APIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let code: String
  
  var path: String { return "/user/verify" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["userId": userId,
                                           "code": code],
                              encoding: JSONEncoding.default)
  }
}

struct AddCoinsRequest: APIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coins: [CoinAddress]
  
  var path: String { return "/user/add-coins" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["userId": userId,
                                           "coins": coins.toJSON()],
                              encoding: JSONEncoding.default)
  }
}

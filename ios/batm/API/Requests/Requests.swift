import Moya

struct RefreshTokenRequest: APIRequest {
  typealias ResponseType = APIResponse<Account>
  typealias ResponseTrait = SingleResponseTrait
  
  let account: Account
  
  var path: String { return "/refresh" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["refreshToken": account.refreshToken],
                              encoding: JSONEncoding.default)
  }
}

struct CreateAccountRequest: APIRequest {
  typealias ResponseType = APIResponse<Account>
  typealias ResponseTrait = SingleResponseTrait
  
  let phoneNumber: String
  let password: String
  
  var path: String { return "/register" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phoneNumber,
                                           "password": password],
                              encoding: JSONEncoding.default)
  }
}

struct RecoverWalletRequest: APIRequest {
  typealias ResponseType = APIResponse<Account>
  typealias ResponseTrait = SingleResponseTrait
  
  let phoneNumber: String
  let password: String
  
  var path: String { return "/recover" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phoneNumber,
                                           "password": password],
                              encoding: JSONEncoding.default)
  }
}

struct VerifyCodeRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let code: String
  
  var path: String { return "/user/\(userId)/verify" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["code": code],
                              encoding: JSONEncoding.default)
  }
}

struct AddCoinsRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinAddresses: [CoinAddress]
  
  var path: String { return "/user/\(userId)/coins/add" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["coins": coinAddresses.toJSON()],
                              encoding: JSONEncoding.default)
  }
}

struct CoinsBalanceRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<CoinsBalance>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coins: [BTMCoin]
  
  var path: String { return "/user/\(userId)/coins/balance" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestParameters(parameters: ["coins": coins.map { $0.type.code }],
                              encoding: URLEncoding.customDefault)
  }
}

struct MapAddressesRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<MapAddresses>
  typealias ResponseTrait = SingleResponseTrait
  
  var path: String { return "/static/atm/address" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct GetPhoneNumberRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<PhoneNumber>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  
  var path: String { return "/user/\(userId)/phone" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct CheckPasswordRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<CheckPassword>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let password: String
  
  var path: String { return "/user/\(userId)/check/password" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["password": password],
                              encoding: JSONEncoding.default)
  }
}

struct ChangePhoneRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let phoneNumber: String
  
  var path: String { return "/user/\(userId)/phone" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phoneNumber],
                              encoding: JSONEncoding.default)
  }
}

struct ConfirmPhoneRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let phoneNumber: String
  let code: String
  
  var path: String { return "/user/\(userId)/phone/confirm" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phoneNumber,
                                           "code": code],
                              encoding: JSONEncoding.default)
  }
}

struct ChangePasswordRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let oldPassword: String
  let newPassword: String
  
  var path: String { return "/user/\(userId)/password" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["oldPassword": oldPassword,
                                           "newPassword": newPassword],
                              encoding: JSONEncoding.default)
  }
}

struct UnlinkRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  
  var path: String { return "/user/\(userId)/unlink" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestPlain
  }
}

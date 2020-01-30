import Moya
import TrustWalletCore

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
  
  var path: String { return "/user/\(userId)/code/verify" }
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

struct CompareCoinsRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinAddresses: [CoinAddress]
  
  var path: String { return "/user/\(userId)/coins/compare" }
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

struct CoinsFeeRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<CoinsFee>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  
  var path: String { return "/user/\(userId)/coins/fee" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct MapAddressesRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<MapAddresses>
  typealias ResponseTrait = SingleResponseTrait
  
  var path: String { return "/terminal/locations" }
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
  
  var path: String { return "/user/\(userId)/password/verify" }
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
  
  var path: String { return "/user/\(userId)/phone/update" }
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
  
  var path: String { return "/user/\(userId)/phone/verify" }
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
  
  var path: String { return "/user/\(userId)/password/update" }
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
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct TransactionsRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<Transactions>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  let index: Int
  
  var path: String { return "/user/\(userId)/coins/\(coinId)/transactions" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestParameters(parameters: ["index": index],
                              encoding: URLEncoding.customDefault)
  }
}

struct TransactionDetailsRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<TransactionDetails>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  let id: String
  
  var path: String { return "/user/\(userId)/coins/\(coinId)/transaction/\(id)" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct UtxosRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<Utxos>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  let xpub: String
  
  var path: String { return "/user/\(userId)/coins/\(coinId)/transactions/utxo/\(xpub)" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct GetNonceRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<Nonce>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  
  var path: String { return "/user/\(userId)/coins/\(coinId)/transactions/nonce" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct PreSubmitTransactionRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<PreSubmitResponse>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  let coinAmount: Double
  let currencyAmount: Double
  
  var path: String { return "/user/\(userId)/coins/\(coinId)/transactions/presubmit" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["cryptoAmount": coinAmount,
                                           "fiatAmount": currencyAmount,
                                           "fiatCurrency": "USD"],
                              encoding: JSONEncoding.default)
  }
}

struct SubmitTransactionRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  let txType: TransactionType
  let amount: Double
  let phone: String?
  let message: String?
  let imageId: String?
  let txhex: String?
  
  var path: String { return "/user/\(userId)/coins/\(coinId)/transactions/submit" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["type": txType.rawValue,
                                           "cryptoAmount": amount,
                                           "phone": phone as Any,
                                           "message": message as Any,
                                           "imageId": imageId as Any,
                                           "hex": txhex as Any],
                              encoding: JSONEncoding.default)
  }
}

struct RequestCodeRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  
  var path: String { return "/user/\(userId)/code/send" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct GetTronBlockHeaderRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<BTMTronBlockHeader>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  
  var path: String { return "/user/\(userId)/coins/\(coinId)/transactions/currentblock" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct GetBinanceAccountInfoRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<BinanceAccountInfo>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  
  var path: String { return "/user/\(userId)/coins/\(coinId)/transactions/currentaccount" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct GetRippleSequenceRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<RippleSequence>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  
  var path: String { return "/user/\(userId)/coins/\(coinId)/transactions/currentaccount" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct GetGiftAddressRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<GiftAddress>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  let phone: String
  
  var path: String { return "/user/\(userId)/coins/\(coinId)/giftaddress" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phone], encoding: URLEncoding.customDefault)
  }
}

struct GetSellAddressRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<SellAddress>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  
  var path: String { return "/user/\(userId)/coins/\(coinId)/transactions/selldetails" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct GetSellDetailsRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<SellDetails>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  
  var path: String { return "/user/\(userId)/coins/\(coinId)/transactions/limits" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

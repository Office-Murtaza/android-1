import Moya
import TrustWalletCore

struct RefreshTokenRequest: RetriableAPIRequest {
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

struct CheckAccountRequest: RetriableAPIRequest {
  typealias ResponseType = APIResponse<CheckAccountResponse>
  typealias ResponseTrait = SingleResponseTrait
  
  let phoneNumber: String
  let password: String
  
  var path: String { return "/check" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phoneNumber,
                                           "password": password],
                              encoding: JSONEncoding.default)
  }
}

struct PhoneVerificationRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<PhoneVerificationResponse>
  typealias ResponseTrait = SingleResponseTrait
  
  let phoneNumber: String
  
  var path: String { return "/verify" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phoneNumber],
                              encoding: JSONEncoding.default)
  }
}

struct CreateAccountRequest: RetriableAPIRequest {
  typealias ResponseType = APIResponse<CreateWalletResponse>
  typealias ResponseTrait = SingleResponseTrait
  
  let phoneNumber: String
  let password: String
  let coinAddresses: [CoinAddress]
  let notificationsToken: String
  private let userTimeZone: String = TimeZone.current.localizedName(for: .standard, locale: nil) ?? ""
  private let locationManager = UserLocationManager()


  var path: String { return "/register" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phoneNumber,
                                           "password": password,
                                           "platform": MobilePlatform.iOS.rawValue,
                                           "deviceModel": UIDevice.current.deviceModel,
                                           "deviceOS": UIDevice.current.deviceOS,
                                           "appVersion": UIApplication.shared.appVersion,
                                           "notificationsToken": notificationsToken,
                                           "latitude": locationManager.getUserLatitude(),
                                           "longitude": locationManager.getUserLongitude(),
                                           "timezone": userTimeZone,
                                           "coins": coinAddresses.toJSON()],
                              encoding: JSONEncoding.default)
  }
}

struct RecoverWalletRequest: RetriableAPIRequest {
  typealias ResponseType = APIResponse<CreateWalletResponse>
  typealias ResponseTrait = SingleResponseTrait
  
  let phoneNumber: String
  let password: String
  let coinAddresses: [CoinAddress]
  let notificationsToken: String
  private let userTimeZone: String = TimeZone.current.localizedName(for: .standard, locale: nil) ?? ""
  private let locationManager = UserLocationManager()

  var path: String { return "/recover" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phoneNumber,
                                           "password": password,
                                           "platform": MobilePlatform.iOS.rawValue,
                                           "deviceModel": UIDevice.current.deviceModel,
                                           "deviceOS": UIDevice.current.deviceOS,
                                           "appVersion": UIApplication.shared.appVersion,
                                           "notificationsToken": notificationsToken,
                                           "latitude": locationManager.getUserLatitude(),
                                           "longitude": locationManager.getUserLongitude(),
                                           "timezone": userTimeZone,
                                           "coins": coinAddresses.toJSON()],
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

struct CoinsBalanceRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<CoinsBalance>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coins: [BTMCoin]
  
  var path: String { return "/user/\(userId)/balance" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
  return .requestParameters(parameters: ["coins": coins.map { $0.type.code }],
                            encoding: URLEncoding.customDefault)
  }
}

struct CoinDetailsRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<CoinDetails>
  typealias ResponseTrait = SingleResponseTrait
  
  let coinId: String
  
  var path: String { return "/coin/\(coinId)/details" }
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

struct VerifyPasswordRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<ResponseResult>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let password: String
  
  var path: String { return "/user/\(userId)/password-verify" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["password": password],
                              encoding: JSONEncoding.default)
  }
}

struct VerifyPhoneRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<ResponseResult>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let phoneNumber: String
  
  var path: String { return "/user/\(userId)/phone-verify" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phoneNumber],
                              encoding: JSONEncoding.default)
  }
}

struct UpdatePhoneRequest: AuthorizedAPIRequest {
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
  
  var path: String { return "/user/\(userId)/phone/verify" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phoneNumber,
                                           "code": code],
                              encoding: JSONEncoding.default)
  }
}

struct UpdatePasswordRequest: AuthorizedAPIRequest {
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
  
  var path: String { return "/user/\(userId)/coin/\(coinId)/transaction-history" }
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
  
  var path: String { return "/user/\(userId)/coin/\(coinId)/transaction-details" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestParameters(parameters: ["txId": id],
                              encoding: URLEncoding.customDefault)
  }
}

struct UtxosRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<Utxos>
  typealias ResponseTrait = SingleResponseTrait
  
  let coinId: String
  let xpub: String
  
  var path: String { return "/coin/\(coinId)/utxo" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestParameters(parameters: ["xpub": xpub],
                              encoding: URLEncoding.customDefault)
  }
}

struct GetNonceRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<Nonce>
  typealias ResponseTrait = SingleResponseTrait
  
  let coinId: String
  let address: String
  
  var path: String { return "/coin/\(coinId)/nonce" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestParameters(parameters: ["address": address],
                              encoding: URLEncoding.customDefault)
  }
}

struct PreSubmitTransactionRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<PreSubmitResponse>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  let coinAmount: Decimal
  let currencyAmount: Decimal
  
  var path: String { return "/user/\(userId)/coin/\(coinId)/pre-submit" }
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
  let amount: Decimal
  let fee: Decimal?
  let fromAddress: String?
  let toAddress: String?
  let phone: String?
  let message: String?
  let imageId: String?
  let toCoinId: String?
  let toCoinAmount: Decimal?
  let txhex: String?
  
  var path: String { return "/user/\(userId)/coin/\(coinId)/submit" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["type": txType.rawValue,
                                           "cryptoAmount": amount,
                                           "fee": fee as Any,
                                           "fromAddress": fromAddress as Any,
                                           "toAddress": toAddress as Any,
                                           "phone": phone as Any,
                                           "message": message as Any,
                                           "imageId": imageId as Any,
                                           "refCoin": toCoinId as Any,
                                           "refCryptoAmount": toCoinAmount as Any,
                                           "hex": txhex as Any],
                              encoding: JSONEncoding.default)
  }
}

struct GetTronBlockHeaderRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<BTMTronBlockHeader>
  typealias ResponseTrait = SingleResponseTrait
  
  let coinId: String
  
  var path: String { return "/coin/\(coinId)/current-block" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
} 

struct GetBinanceAccountInfoRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<BinanceAccountInfo>
  typealias ResponseTrait = SingleResponseTrait
  
  let coinId: String
  let address: String
  
  var path: String { return "/coin/\(coinId)/current-account" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestParameters(parameters: ["address": address], encoding: URLEncoding.customDefault)
  }
}

struct GetRippleSequenceRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<RippleSequence>
  typealias ResponseTrait = SingleResponseTrait
  
  let coinId: String
  let address: String
  
  var path: String { return "/coin/\(coinId)/current-account" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestParameters(parameters: ["address": address], encoding: URLEncoding.customDefault)
  }
}

struct GetGiftAddressRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<GiftAddress>
  typealias ResponseTrait = SingleResponseTrait
  
  let coinId: String
  let phone: String
  
  var path: String { return "/coin/\(coinId)/transfer-address" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestParameters(parameters: ["phone": phone], encoding: URLEncoding.customDefault)
  }
}

struct GetCurrentAccountActivatedRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<ResponseResult>
  typealias ResponseTrait = SingleResponseTrait
  
  let coinId: String
  let address: String
  
  var path: String { return "/coin/\(coinId)/current-account-activated" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestParameters(parameters: ["address": address], encoding: URLEncoding.customDefault)
  }
}

struct GetSellDetailsRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<SellDetails>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  
  var path: String { return "/user/\(userId)/limits" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct KYCRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<KYC>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  
  var path: String { return "/user/\(userId)/kyc-details" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct SendVerificationRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let userData: VerificationUserData
  
  var path: String { return "/user/\(userId)/kyc-submit" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .uploadMultipart([
      MultipartFormData(provider: .data(userData.tierId.data(using: .utf8)!), name: "tierId"),
      MultipartFormData(provider: .data(userData.idNumber.data(using: .utf8)!), name: "idNumber"),
      MultipartFormData(provider: .data(userData.firstName.data(using: .utf8)!), name: "firstName"),
      MultipartFormData(provider: .data(userData.lastName.data(using: .utf8)!), name: "lastName"),
      MultipartFormData(provider: .data(userData.address.data(using: .utf8)!), name: "address"),
      MultipartFormData(provider: .data(userData.country.data(using: .utf8)!), name: "country"),
      MultipartFormData(provider: .data(userData.province.data(using: .utf8)!), name: "province"),
      MultipartFormData(provider: .data(userData.city.data(using: .utf8)!), name: "city"),
      MultipartFormData(provider: .data(userData.zipCode.data(using: .utf8)!), name: "zipCode"),
      MultipartFormData(provider: .data(userData.scanData), name: "file", fileName: "id_scan.png", mimeType: "image/png"),
    ])
  }
}

struct SendVIPVerificationRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let userData: VIPVerificationUserData
  
  var path: String { return "/user/\(userId)/kyc-submit" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .uploadMultipart([
      MultipartFormData(provider: .data(userData.tierId.data(using: .utf8)!), name: "tierId"),
      MultipartFormData(provider: .data(userData.ssn.data(using: .utf8)!), name: "ssn"),
      MultipartFormData(provider: .data(userData.selfieData), name: "file", fileName: "id_selfie.png", mimeType: "image/png"),
    ])
  }
}

struct GetPriceChartDataRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<PriceChartData>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  
  var path: String { return "/user/\(userId)/coin/\(coinId)/price-chart" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct GetPriceChartDetailsRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<PriceChartDetails>
  typealias ResponseTrait = SingleResponseTrait

  let coinId: String
  let coinPeriod: SelectedPeriod
  var path: String { return "/coin/\(coinId)/price-chart" }
  var method: HTTPMethod { .get }
  var task: HTTPTask {
    return .requestParameters(parameters: [
      "period" : coinPeriod.rawValue
    ], encoding: URLEncoding.customDefault)}
}

struct BuySellTradesRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<BuySellTrades>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  let type: TradeType
  let index: Int
  
  var path: String { return "/user/\(userId)/coin/\(coinId)/trade-history" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestParameters(parameters: ["tab": type.rawValue,
                                           "index": index,
                                           "sort": 1],
                              encoding: URLEncoding.customDefault)
  }
}

struct UpdateLocationRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let latitude: Double
  let longitude: Double
  
  var path: String { return "/user/\(userId)/location" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["latitude": latitude,
                                           "longitude": longitude],
                              encoding: JSONEncoding.default)
  }
}

struct SubmitTradeRequestRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let data: SubmitTradeRequestData
  
  var path: String { return "/user/\(userId)/coin/\(data.coinType.code)/trade-request" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["tradeId": data.trade.id,
                                           "price": data.trade.price,
                                           "cryptoAmount": data.coinAmount,
                                           "fiatAmount": data.currencyAmount,
                                           "details": data.details],
                              encoding: JSONEncoding.default)
  }
}

struct SubmitTradeRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let data: SubmitTradeData
  
  var path: String { return "/user/\(userId)/coin/\(data.coinType.code)/trade" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: ["type": data.tradeType.rawValue,
                                           "paymentMethod": data.payment,
                                           "margin": data.margin,
                                           "minLimit": data.minLimit,
                                           "maxLimit": data.maxLimit,
                                           "terms": data.terms],
                              encoding: JSONEncoding.default)
  }
}

struct StakeDetailsRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<StakeDetails>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  
  var path: String { return "/user/\(userId)/coin/\(coinId)/stake-details" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
    return .requestPlain
  }
}

struct ManageCoinsRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIEmptyResponse
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let coinId: String
  let isVisible: String
  
  var path: String { return "/user/\(userId)/coin/\(coinId)/manage" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
      return .requestParameters(parameters: ["enabled": isVisible],
                                encoding: URLEncoding.customDefault)
  }
}

struct TradesRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<Trades>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  
  var path: String { return "/user/\(userId)/trade-history" }
  var method: HTTPMethod { return .get }
  var task: HTTPTask {
      return .requestPlain
  }
}

struct CreateTradesRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<Trade>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let data: P2PCreateTradeDataModel
  
  var path: String { return "/user/\(userId)/trade" }
  var method: HTTPMethod { return .post }
  var task: HTTPTask {
    return .requestParameters(parameters: data.dictionary,
                              encoding: JSONEncoding.default)
  }
}

struct EditTradesRequest: AuthorizedAPIRequest {
  typealias ResponseType = APIResponse<Trade>
  typealias ResponseTrait = SingleResponseTrait
  
  let userId: Int
  let data: P2PEditTradeDataModel
  
  var path: String { return "/user/\(userId)/trade" }
  var method: HTTPMethod { return .put }
  var task: HTTPTask {
    return .requestParameters(parameters: data.dictionary,
                              encoding: JSONEncoding.default)
  }
}

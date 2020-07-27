import Foundation
import TrustWalletCore
import RxSwift
import ObjectMapper

enum APIError: Error, Equatable {
  case noConnection
  case networkError
  case notAuthorized
  case forbidden
  case notFound
  case conflict
  case notValid
  case unknown
  case serverError(ServerError)
}

protocol APIGateway {
  
  func checkAccount(phoneNumber: String, password: String) -> Single<CheckAccountResponse>
  func verifyPhone(phoneNumber: String) -> Single<PhoneVerificationResponse>
  func createAccount(phoneNumber: String, password: String, coinAddresses: [CoinAddress]) -> Single<Account>
  func recoverWallet(phoneNumber: String, password: String, coinAddresses: [CoinAddress]) -> Single<Account>
  func verifyCode(userId: Int, code: String) -> Completable
  func getCoinsBalance(userId: Int, coins: [BTMCoin]) -> Single<CoinsBalance>
  func getCoinSettings(type: CustomCoinType) -> Single<CoinSettings>
  func getMapAddresses() -> Single<MapAddresses>
  func getPhoneNumber(userId: Int) -> Single<PhoneNumber>
  func checkPassword(userId: Int, password: String) -> Single<Bool>
  func changePhone(userId: Int, phoneNumber: String) -> Completable
  func confirmPhone(userId: Int, phoneNumber: String, code: String) -> Completable
  func changePassword(userId: Int, oldPassword: String, newPassword: String) -> Completable
  func unlink(userId: Int) -> Completable
  func getTransactions(userId: Int, type: CustomCoinType, page: Int) -> Single<Transactions>
  func getTransactionDetails(userId: Int, type: CustomCoinType, id: String) -> Single<TransactionDetails>
  func getUtxos(type: CustomCoinType, xpub: String) -> Single<[Utxo]>
  func presubmitTransaction(userId: Int,
                            type: CustomCoinType,
                            coinAmount: Double,
                            currencyAmount: Double) -> Single<PreSubmitResponse>
  func submitTransaction(userId: Int,
                         type: CustomCoinType,
                         txType: TransactionType,
                         amount: Double,
                         fee: Double?,
                         fromAddress: String?,
                         toAddress: String?,
                         phone: String?,
                         message: String?,
                         imageId: String?,
                         toCoinType: CustomCoinType?,
                         txhex: String?) -> Completable
  func getTronBlockHeader(type: CustomCoinType) -> Single<BTMTronBlockHeader>
  func getGiftAddress(type: CustomCoinType, phone: String) -> Single<GiftAddress>
  func getNonce(type: CustomCoinType, address: String) -> Single<Nonce>
  func getBinanceAccountInfo(type: CustomCoinType, address: String) -> Single<BinanceAccountInfo>
  func getRippleSequence(type: CustomCoinType, address: String) -> Single<RippleSequence>
  func getSellAddress(userId: Int, type: CustomCoinType) -> Single<SellAddress>
  func getSellDetails(userId: Int) -> Single<SellDetails>
  func getVerificationInfo(userId: Int) -> Single<VerificationInfo>
  func sendVerification(userId: Int, userData: VerificationUserData) -> Completable
  func sendVIPVerification(userId: Int, userData: VIPVerificationUserData) -> Completable
  func getPriceChartData(userId: Int, type: CustomCoinType) -> Single<PriceChartData>
  func getBuyTrades(userId: Int, type: CustomCoinType, page: Int) -> Single<BuySellTrades>
  func getSellTrades(userId: Int, type: CustomCoinType, page: Int) -> Single<BuySellTrades>
  func updateLocation(userId: Int, latitude: Double, longitude: Double) -> Completable
  func submitTradeRequest(userId: Int, data: SubmitTradeRequestData) -> Completable
  func submitTrade(userId: Int, data: SubmitTradeData) -> Completable
  func getStakeDetails(userId: Int, type: CustomCoinType) -> Single<StakeDetails>
}

final class APIGatewayImpl: APIGateway {
  
  let api: NetworkRequestExecutor
  
  required init(networkProvider api: NetworkRequestExecutor) {
    self.api = api
  }
  
  func execute<Response: ImmutableMappable, Request: APIRequest>(_ request: Request) -> Single<Response>
    where Request.ResponseType == APIResponse<Response>, Request.ResponseTrait == SingleResponseTrait {
      return api.execute(request)
        .flatMap {
          switch $0 {
          case let .response(response):
            return Single.just(response)
          case let .error(error):
            return Single.error(error)
          }
        }
  }
  
  func execute<Request: APIRequest>(_ request: Request) -> Completable
    where Request.ResponseType == APIEmptyResponse, Request.ResponseTrait == SingleResponseTrait {
      return api.execute(request)
        .map { apiResponse -> Void in
          switch apiResponse {
          case .response:
            return Void()
          case let .error(error):
            throw error
          }
        }
        .toCompletable()
  }
  
  func checkAccount(phoneNumber: String, password: String) -> Single<CheckAccountResponse> {
    let request = CheckAccountRequest(phoneNumber: phoneNumber, password: password)
    return execute(request)
  }
  
  func verifyPhone(phoneNumber: String) -> Single<PhoneVerificationResponse> {
    let request = PhoneVerificationRequest(phoneNumber: phoneNumber)
    return execute(request)
  }
  
  func createAccount(phoneNumber: String, password: String, coinAddresses: [CoinAddress]) -> Single<Account> {
    let request = CreateAccountRequest(phoneNumber: phoneNumber, password: password, coinAddresses: coinAddresses)
    return execute(request)
  }
  
  func recoverWallet(phoneNumber: String, password: String, coinAddresses: [CoinAddress]) -> Single<Account> {
    let request = RecoverWalletRequest(phoneNumber: phoneNumber, password: password, coinAddresses: coinAddresses)
    return execute(request)
  }
  
  func verifyCode(userId: Int, code: String) -> Completable {
    let request = VerifyCodeRequest(userId: userId, code: code)
    return execute(request)
  }
  
  func getCoinsBalance(userId: Int, coins: [BTMCoin]) -> Single<CoinsBalance> {
    if coins.isEmpty {
      return Single.just(CoinsBalance(totalBalance: 0, coins: []))
    }
    
    let request = CoinsBalanceRequest(userId: userId, coins: coins)
    return execute(request)
  }
  
  func getCoinSettings(type: CustomCoinType) -> Single<CoinSettings> {
    let request = CoinSettingsRequest(coinId: type.code)
    return execute(request)
  }
  
  func getMapAddresses() -> Single<MapAddresses> {
    let request = MapAddressesRequest()
    return execute(request)
  }
  
  func getPhoneNumber(userId: Int) -> Single<PhoneNumber> {
    let request = GetPhoneNumberRequest(userId: userId)
    return execute(request)
  }
  
  func checkPassword(userId: Int, password: String) -> Single<Bool> {
    let request = CheckPasswordRequest(userId: userId, password: password)
    return execute(request).map { $0.result }
  }
  
  func changePhone(userId: Int, phoneNumber: String) -> Completable {
    let request = ChangePhoneRequest(userId: userId, phoneNumber: phoneNumber)
    return execute(request)
  }
  
  func confirmPhone(userId: Int, phoneNumber: String, code: String) -> Completable {
    let request = ConfirmPhoneRequest(userId: userId, phoneNumber: phoneNumber, code: code)
    return execute(request)
  }
  
  func changePassword(userId: Int, oldPassword: String, newPassword: String) -> Completable {
    let request = ChangePasswordRequest(userId: userId, oldPassword: oldPassword, newPassword: newPassword)
    return execute(request)
  }
  
  func unlink(userId: Int) -> Completable {
    let request = UnlinkRequest(userId: userId)
    return execute(request)
  }
  
  func getTransactions(userId: Int, type: CustomCoinType, page: Int) -> Single<Transactions> {
    let index = page * 10 + 1
    let request = TransactionsRequest(userId: userId, coinId: type.code, index: index)
    return execute(request)
  }
  
  func getTransactionDetails(userId: Int, type: CustomCoinType, id: String) -> Single<TransactionDetails> {
    let request = TransactionDetailsRequest(userId: userId, coinId: type.code, id: id)
    return execute(request)
  }
  
  func getUtxos(type: CustomCoinType, xpub: String) -> Single<[Utxo]> {
    let request = UtxosRequest(coinId: type.code, xpub: xpub)
    return execute(request).map { $0.utxos }
  }
  
  func presubmitTransaction(userId: Int,
                            type: CustomCoinType,
                            coinAmount: Double,
                            currencyAmount: Double) -> Single<PreSubmitResponse> {
    let request = PreSubmitTransactionRequest(userId: userId,
                                              coinId: type.code,
                                              coinAmount: coinAmount,
                                              currencyAmount: currencyAmount)
    return execute(request)
  }
  
  func submitTransaction(userId: Int,
                         type: CustomCoinType,
                         txType: TransactionType,
                         amount: Double,
                         fee: Double?,
                         fromAddress: String?,
                         toAddress: String?,
                         phone: String?,
                         message: String?,
                         imageId: String?,
                         toCoinType: CustomCoinType?,
                         txhex: String?) -> Completable {
    let request = SubmitTransactionRequest(userId: userId,
                                           coinId: type.code,
                                           txType: txType,
                                           amount: amount,
                                           fee: fee,
                                           fromAddress: fromAddress,
                                           toAddress: toAddress,
                                           phone: phone,
                                           message: message,
                                           imageId: imageId,
                                           toCoinId: toCoinType?.code,
                                           txhex: txhex)
    return execute(request)
  }
  
  func getTronBlockHeader(type: CustomCoinType) -> Single<BTMTronBlockHeader> {
    let request = GetTronBlockHeaderRequest(coinId: type.code)
    return execute(request)
  }
  
  func getGiftAddress(type: CustomCoinType, phone: String) -> Single<GiftAddress> {
    let request = GetGiftAddressRequest(coinId: type.code, phone: phone)
    return execute(request)
  }
  
  func getNonce(type: CustomCoinType, address: String) -> Single<Nonce> {
    let request = GetNonceRequest(coinId: type.code, address: address)
    return execute(request)
  }
  
  func getBinanceAccountInfo(type: CustomCoinType, address: String) -> Single<BinanceAccountInfo> {
    let request = GetBinanceAccountInfoRequest(coinId: type.code, address: address)
    return execute(request)
  }
  
  func getRippleSequence(type: CustomCoinType, address: String) -> Single<RippleSequence> {
    let request = GetRippleSequenceRequest(coinId: type.code, address: address)
    return execute(request)
  }
  
  func getSellAddress(userId: Int, type: CustomCoinType) -> Single<SellAddress> {
    let request = GetSellAddressRequest(userId: userId, coinId: type.code)
    return execute(request)
  }
  
  func getSellDetails(userId: Int) -> Single<SellDetails> {
    let request = GetSellDetailsRequest(userId: userId)
    return execute(request)
  }
  
  func getVerificationInfo(userId: Int) -> Single<VerificationInfo> {
    let request = GetVerificationInfoRequest(userId: userId)
    return execute(request)
  }
  
  func sendVerification(userId: Int, userData: VerificationUserData) -> Completable {
    let request = SendVerificationRequest(userId: userId, userData: userData)
    return execute(request)
  }
  
  func sendVIPVerification(userId: Int, userData: VIPVerificationUserData) -> Completable {
    let request = SendVIPVerificationRequest(userId: userId, userData: userData)
    return execute(request)
  }
  
  func getPriceChartData(userId: Int, type: CustomCoinType) -> Single<PriceChartData> {
    let request = GetPriceChartDataRequest(userId: userId, coinId: type.code)
    return execute(request)
  }
  
  func getBuyTrades(userId: Int, type: CustomCoinType, page: Int) -> Single<BuySellTrades> {
    let index = page * 10 + 1
    let request = BuySellTradesRequest(userId: userId, coinId: type.code, type: TradeType.buy, index: index)
    return execute(request)
  }
  
  func getSellTrades(userId: Int, type: CustomCoinType, page: Int) -> Single<BuySellTrades> {
    let index = page * 10 + 1
    let request = BuySellTradesRequest(userId: userId, coinId: type.code, type: TradeType.sell, index: index)
    return execute(request)
  }
  
  func updateLocation(userId: Int, latitude: Double, longitude: Double) -> Completable {
    let request = UpdateLocationRequest(userId: userId, latitude: latitude, longitude: longitude)
    return execute(request)
  }
  
  func submitTradeRequest(userId: Int, data: SubmitTradeRequestData) -> Completable {
    let request = SubmitTradeRequestRequest(userId: userId, data: data)
    return execute(request)
  }
  
  func submitTrade(userId: Int, data: SubmitTradeData) -> Completable {
    let request = SubmitTradeRequest(userId: userId, data: data)
    return execute(request)
  }
  
  func getStakeDetails(userId: Int, type: CustomCoinType) -> Single<StakeDetails> {
    let request = StakeDetailsRequest(userId: userId, coinId: type.code)
    return execute(request)
  }
  
}


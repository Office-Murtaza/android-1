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
  func createAccount(phoneNumber: String, password: String, coinAddresses: [CoinAddress]) -> Single<CreateWalletResponse>
  func recoverWallet(phoneNumber: String, password: String, coinAddresses: [CoinAddress]) -> Single<CreateWalletResponse>
  func verifyCode(userId: String, code: String) -> Completable
  func getMapAddresses() -> Single<MapAddresses>
  func getPhoneNumber(userId: String) -> Single<PhoneNumber>
  func verifyPassword(userId: String, password: String) -> Single<Bool>
  func verifyPhone(userId: String, phoneNumber: String) -> Single<Bool>
  func updatePhone(userId: String, phoneNumber: String) -> Completable
  func confirmPhone(userId: String, phoneNumber: String, code: String) -> Completable
  func updatePassword(userId: String, oldPassword: String, newPassword: String) -> Completable
  func getTransactions(userId: String, type: CustomCoinType, page: Int) -> Single<Transactions>
  func getTransactionDetails(userId: String, type: CustomCoinType, id: String) -> Single<TransactionDetails>
  func getUtxos(type: CustomCoinType, xpub: String) -> Single<[Utxo]>
  func presubmitTransaction(userId: String,
                            type: CustomCoinType,
                            coinAmount: Decimal,
                            currencyAmount: Decimal) -> Single<PreSubmitResponse>
    func submitCoinTransaction(userId: String,
                               type: CustomCoinType,
                               txType: TransactionType,
                               amount: Decimal,
                               fee: Decimal?,
                               fromAddress: String?,
                               toAddress: String?,
                               phone: String?,
                               message: String?,
                               image: String?,
                               toCoinType: CustomCoinType?,
                               toCoinAmount: Decimal?,
                               txhex: String?,
                               from screen: ScreenType) -> Single<TransactionDetails>

  func submitTransaction(userId: String,
                         type: CustomCoinType,
                         txType: TransactionType,
                         amount: Decimal,
                         fee: Decimal?,
                         fromAddress: String?,
                         toAddress: String?,
                         phone: String?,
                         message: String?,
                         image: String?,
                         toCoinType: CustomCoinType?,
                         toCoinAmount: Decimal?,
                         txhex: String?,
                         from screen: ScreenType) -> Completable
  func getTronBlockHeader(type: CustomCoinType) -> Single<BTMTronBlockHeader>
  func getGiftAddress(type: CustomCoinType, phone: String) -> Single<GiftAddress>
  func getNonce(address: String) -> Single<Nonce>
  func getBinanceAccountInfo(type: CustomCoinType, address: String) -> Single<BinanceAccountInfo>
  func getRippleSequence(type: CustomCoinType, address: String) -> Single<RippleSequence>
  func getCurrentAccountActivated(address: String) -> Single<Bool>
  func getSellDetails(userId: String) -> Single<SellDetails>
  func getKYC(userId: String) -> Single<KYC>
  func sendVerification(userId: String, userData: VerificationUserData) -> Completable
  func sendVIPVerification(userId: String, userData: VIPVerificationUserData) -> Completable
  func getBuyTrades(userId: String, type: CustomCoinType, page: Int) -> Single<BuySellTrades>
  func getSellTrades(userId: String, type: CustomCoinType, page: Int) -> Single<BuySellTrades>
  func updateLocation(userId: String, latitude: Double, longitude: Double) -> Completable
  func submitTradeRequest(userId: String, data: SubmitTradeRequestData) -> Completable
  func submitTrade(userId: String, data: SubmitTradeData) -> Completable
  func getStakeDetails(userId: String, type: CustomCoinType) -> Single<StakeDetails>
  func manageCoins(userId: String, coin: String, visible: Bool) -> Single<Bool>
  func getPriceChart(type: CustomCoinType, period: SelectedPeriod) -> Single<PriceChartDetails>
  func getTrades(userId: String) -> Single<Trades>
  func createTrade(userId: String, data: P2PCreateTradeDataModel) -> Single<Trade>
  func editTrade(userId: String, data: P2PEditTradeDataModel) -> Single<Trade>
  func cancelTrade(userId: String, id: String) -> Single<Trade>
  func createOrder(userId: String, tradeId: String, price: Double, cryptoAmount: Double, fiatAmount: Double) -> Single<Order>
  func cancelOrder(userId: String, id: String) -> Single<Order>
  func updateOrder(userId: String, id: String, status: OrderDetailsActionType, rate: Int?) -> Single<Order>
  func updateRate(userId: String, id: String, rate: Int) -> Single<Order>
}

final class APIGatewayImpl: APIGateway {
  
  let api: NetworkRequestExecutor
  let errorService: ErrorService
  
  required init(networkProvider api: NetworkRequestExecutor,
                errorService: ErrorService) {
    self.api = api
    self.errorService = errorService
  }
  
    private func processError<T>(_ error: APIError, from screen: ScreenType) -> Single<T> {
        if case let .serverError(serverError) = error, serverError.code == 1, screen == .none {
            return errorService.showError(for: .serverError).andThen(.error(error))
        }
    
        return Single.error(error)
  }
  
  func execute<Response: ImmutableMappable, Request: APIRequest>(_ request: Request, from screen: ScreenType = .none) -> Single<Response>
    where Request.ResponseType == APIResponse<Response>, Request.ResponseTrait == SingleResponseTrait {
      return api.execute(request)
        .flatMap { [unowned self] in
          switch $0 {
          case let .response(response):
            return Single.just(response)
          case let .error(error):
            return self.processError(error, from: screen)
          }
        }
  }
   
  func execute<Request: APIRequest>(_ request: Request, from screen: ScreenType = .none) -> Completable
    where Request.ResponseType == APIEmptyResponse, Request.ResponseTrait == SingleResponseTrait {
      return api.execute(request)
        .flatMap { [unowned self] apiResponse -> Single<Void> in
          switch apiResponse {
          case .response:
            return Single.just(())
          case let .error(error):
            return self.processError(error, from: screen)
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
  
  func createAccount(phoneNumber: String, password: String, coinAddresses: [CoinAddress]) -> Single<CreateWalletResponse> {
    let token = KeychainManager.loadValue(for: GlobalConstants.fcmPushToken)
    let request = CreateAccountRequest(phoneNumber: phoneNumber, password: password, coinAddresses: coinAddresses, notificationsToken: token ?? "")
    return execute(request)
  }
  
  func recoverWallet(phoneNumber: String, password: String, coinAddresses: [CoinAddress]) -> Single<CreateWalletResponse> {
    let token = KeychainManager.loadValue(for: GlobalConstants.fcmPushToken)
    let request = RecoverWalletRequest(phoneNumber: phoneNumber, password: password, coinAddresses: coinAddresses, notificationsToken: token ?? "")
    return execute(request)
  }
  
  func verifyCode(userId: String, code: String) -> Completable {
    let request = VerifyCodeRequest(userId: userId, code: code)
    return execute(request)
  }
  
  func getMapAddresses() -> Single<MapAddresses> {
    let request = MapAddressesRequest()
    return execute(request)
  }
  
  func getPhoneNumber(userId: String) -> Single<PhoneNumber> {
    let request = GetPhoneNumberRequest(userId: userId)
    return execute(request)
  }
  
  func verifyPassword(userId: String, password: String) -> Single<Bool> {
    let request = VerifyPasswordRequest(userId: userId, password: password)
    return execute(request).map { $0.result }
  }
  
  func verifyPhone(userId: String, phoneNumber: String) -> Single<Bool> {
    let request = VerifyPhoneRequest(userId: userId, phoneNumber: phoneNumber)
    return execute(request).map { $0.result }
  }
  
  func updatePhone(userId: String, phoneNumber: String) -> Completable {
    let request = UpdatePhoneRequest(userId: userId, phoneNumber: phoneNumber)
    return execute(request)
  }
  
  func confirmPhone(userId: String, phoneNumber: String, code: String) -> Completable {
    let request = ConfirmPhoneRequest(userId: userId, phoneNumber: phoneNumber, code: code)
    return execute(request)
  }
  
  func updatePassword(userId: String, oldPassword: String, newPassword: String) -> Completable {
    let request = UpdatePasswordRequest(userId: userId, oldPassword: oldPassword, newPassword: newPassword)
    return execute(request)
  }
  
  func getTransactions(userId: String, type: CustomCoinType, page: Int) -> Single<Transactions> {
    let index = page * 10 + 1
    let request = TransactionsRequest(userId: userId, coinId: type.code, index: index)
    return execute(request)
  }
  
  func getTransactionDetails(userId: String, type: CustomCoinType, id: String) -> Single<TransactionDetails> {
    let request = TransactionDetailsRequest(userId: userId, coinId: type.code, id: id)
    return execute(request)
  }
  
  func getUtxos(type: CustomCoinType, xpub: String) -> Single<[Utxo]> {
    let request = UtxosRequest(coinId: type.code, xpub: xpub)
    return execute(request).map { $0.utxos }
  }
  
  func presubmitTransaction(userId: String,
                            type: CustomCoinType,
                            coinAmount: Decimal,
                            currencyAmount: Decimal) -> Single<PreSubmitResponse> {
    let request = PreSubmitTransactionRequest(userId: userId,
                                              coinId: type.code,
                                              coinAmount: coinAmount,
                                              currencyAmount: currencyAmount)
    return execute(request)
  }
    
    func submitCoinTransaction(userId: String,
                               type: CustomCoinType,
                               txType: TransactionType,
                               amount: Decimal,
                               fee: Decimal?,
                               fromAddress: String?,
                               toAddress: String?,
                               phone: String?,
                               message: String?,
                               image: String?,
                               toCoinType: CustomCoinType?,
                               toCoinAmount: Decimal?,
                               txhex: String?,
                               from screen: ScreenType) -> Single<TransactionDetails> {
        let request = CoinSubmitTransactionRequest(userId: userId,
                                                   coinId: type.code,
                                                   txType: txType,
                                                   amount: amount,
                                                   fee: fee,
                                                   fromAddress: fromAddress,
                                                   toAddress: toAddress,
                                                   phone: phone,
                                                   message: message,
                                                   image: image,
                                                   toCoinId: toCoinType?.code,
                                                   toCoinAmount: toCoinAmount,
                                                   txhex: txhex)
        return execute(request, from: screen)
    }

  
  func submitTransaction(userId: String,
                         type: CustomCoinType,
                         txType: TransactionType,
                         amount: Decimal,
                         fee: Decimal?,
                         fromAddress: String?,
                         toAddress: String?,
                         phone: String?,
                         message: String?,
                         image: String?,
                         toCoinType: CustomCoinType?,
                         toCoinAmount: Decimal?,
                         txhex: String?,
                         from screen: ScreenType) -> Completable {
    let request = SubmitTransactionRequest(userId: userId,
                                           coinId: type.code,
                                           txType: txType,
                                           amount: amount,
                                           fee: fee,
                                           fromAddress: fromAddress,
                                           toAddress: toAddress,
                                           phone: phone,
                                           message: message,
                                           image: image,
                                           toCoinId: toCoinType?.code,
                                           toCoinAmount: toCoinAmount,
                                           txhex: txhex)
    return execute(request, from: screen)
  }
  
  func getTronBlockHeader(type: CustomCoinType) -> Single<BTMTronBlockHeader> {
    let request = GetTronBlockHeaderRequest()
    return execute(request)
  }
  
  func getGiftAddress(type: CustomCoinType, phone: String) -> Single<GiftAddress> {
    let request = GetGiftAddressRequest(coinId: type.code, phone: phone)
    return execute(request)
  }
  
  func getNonce(address: String) -> Single<Nonce> {
    let request = GetNonceRequest(address: address)
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
  
  func getCurrentAccountActivated(address: String) -> Single<Bool> {
    let request = GetCurrentAccountActivatedRequest(address: address)
    return execute(request).map { $0.result }
  }
  
  func getSellDetails(userId: String) -> Single<SellDetails> {
    let request = GetSellDetailsRequest(userId: userId)
    return execute(request)
  }
  
  func getKYC(userId: String) -> Single<KYC> {
    let request = KYCRequest(userId: userId)
    return execute(request)
  }
  
  func sendVerification(userId: String, userData: VerificationUserData) -> Completable {
    let request = SendVerificationRequest(userId: userId, userData: userData)
    return execute(request)
  }
  
  func sendVIPVerification(userId: String, userData: VIPVerificationUserData) -> Completable {
    let request = SendVIPVerificationRequest(userId: userId, userData: userData)
    return execute(request)
  }

  func getPriceChart(type: CustomCoinType, period: SelectedPeriod) -> Single<PriceChartDetails> {
    let request = GetPriceChartDetailsRequest(coinId: type.code, coinPeriod: period)
    return execute(request)
  }
  
  func getBuyTrades(userId: String, type: CustomCoinType, page: Int) -> Single<BuySellTrades> {
    let index = page * 10 + 1
    let request = BuySellTradesRequest(userId: userId, coinId: type.code, type: TradeType.buy, index: index)
    return execute(request)
  }
  
  func getSellTrades(userId: String, type: CustomCoinType, page: Int) -> Single<BuySellTrades> {
    let index = page * 10 + 1
    let request = BuySellTradesRequest(userId: userId, coinId: type.code, type: TradeType.sell, index: index)
    return execute(request)
  }
  
  func updateLocation(userId: String, latitude: Double, longitude: Double) -> Completable {
    let request = UpdateLocationRequest(userId: userId, latitude: latitude, longitude: longitude)
    return execute(request)
  }
  
  func submitTradeRequest(userId: String, data: SubmitTradeRequestData) -> Completable {
    let request = SubmitTradeRequestRequest(userId: userId, data: data)
    return execute(request)
  }
  
  func submitTrade(userId: String, data: SubmitTradeData) -> Completable {
    let request = SubmitTradeRequest(userId: userId, data: data)
    return execute(request)
  }
  
  func getStakeDetails(userId: String, type: CustomCoinType) -> Single<StakeDetails> {
    let request = StakeDetailsRequest(userId: userId, coinId: type.code)
    return execute(request)
  }
  
  func manageCoins(userId: String, coin: String, visible: Bool) -> Single<Bool> {
    let request = ManageCoinsRequest(userId: userId, coinId: coin, isVisible: String(visible))
    return execute(request).map { $0.result }
  }
    
  func getTrades(userId: String) -> Single<Trades> {
    let request = TradesRequest(userId: userId)
    return execute(request)
  }
    
  func createTrade(userId: String, data: P2PCreateTradeDataModel) -> Single<Trade> {
    let request = CreateTradesRequest(userId: userId, data: data)
    return execute(request)
  }
  
  func editTrade(userId: String, data: P2PEditTradeDataModel) -> Single<Trade> {
    let request = EditTradesRequest(userId: userId, data: data)
    return execute(request)
  }
  
  func cancelTrade(userId: String, id: String) -> Single<Trade> {
    let request = CancelTradesRequest(userId: userId, id: id)
    return execute(request)
  }
  
    func createOrder(userId: String, tradeId: String, price: Double, cryptoAmount: Double, fiatAmount: Double) -> Single<Order> {
      
    let request = CreateOrderRequest(userId: userId,
                                     tradeId: tradeId,
                                     price: Decimal(string: price.formatted() ?? "0") ?? 0,
                                     cryptoAmount: Decimal(string: cryptoAmount.formatted() ?? "0") ?? 0,
                                     fiatAmount: Decimal(string: fiatAmount.formatted() ?? "0") ?? 0)
        
    return execute(request)
  }
  
  func cancelOrder(userId: String, id: String) -> Single<Order> {
    let request = CancelOrderRequest(userId: userId, id: id)
    return execute(request)
  }
  
  func updateOrder(userId: String, id: String, status: OrderDetailsActionType, rate: Int?) -> Single<Order> {
    let request = UpdateOrderRequest(userId: userId, orderId: id, status: status.networkType, rate: rate)
    return execute(request)
  }
  
  func updateRate(userId: String, id: String, rate: Int) -> Single<Order> {
    let request = UpdateOrderRateRequest(userId: userId, orderId: id, rate: rate)
    return execute(request)
  }
  
}

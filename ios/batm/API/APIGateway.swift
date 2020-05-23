import Foundation
import TrustWalletCore
import RxSwift

enum APIError: Error, Equatable {
  case noConnection
  case networkError
  case notAuthorized
  case forbidden
  case notFound
  case conflict
  case notValid
  case unknown
  case serverError(String)
}

protocol APIGateway {
  
  func createAccount(phoneNumber: String, password: String) -> Single<Account>
  func recoverWallet(phoneNumber: String, password: String) -> Single<Account>
  func verifyCode(userId: Int, code: String) -> Completable
  func addCoins(userId: Int, coinAddresses: [CoinAddress]) -> Completable
  func compareCoins(userId: Int, coinAddresses: [CoinAddress]) -> Completable
  func getCoinsBalance(userId: Int, coins: [BTMCoin]) -> Single<CoinsBalance>
  func getCoinSettings(type: CoinType) -> Single<CoinSettings>
  func getMapAddresses() -> Single<MapAddresses>
  func getPhoneNumber(userId: Int) -> Single<PhoneNumber>
  func checkPassword(userId: Int, password: String) -> Single<Bool>
  func changePhone(userId: Int, phoneNumber: String) -> Completable
  func confirmPhone(userId: Int, phoneNumber: String, code: String) -> Completable
  func changePassword(userId: Int, oldPassword: String, newPassword: String) -> Completable
  func unlink(userId: Int) -> Completable
  func getTransactions(userId: Int, type: CoinType, page: Int) -> Single<Transactions>
  func getTransactionDetails(userId: Int, type: CoinType, id: String) -> Single<TransactionDetails>
  func getUtxos(userId: Int, type: CoinType, xpub: String) -> Single<[Utxo]>
  func presubmitTransaction(userId: Int,
                            type: CoinType,
                            coinAmount: Double,
                            currencyAmount: Double) -> Single<PreSubmitResponse>
  func submitTransaction(userId: Int,
                         type: CoinType,
                         txType: TransactionType,
                         amount: Double,
                         phone: String?,
                         message: String?,
                         imageId: String?,
                         toCoinType: CoinType?,
                         txhex: String?) -> Completable
  func requestCode(userId: Int) -> Completable
  func getTronBlockHeader(userId: Int, type: CoinType) -> Single<BTMTronBlockHeader>
  func getGiftAddress(userId: Int, type: CoinType, phone: String) -> Single<GiftAddress>
  func getNonce(userId: Int, type: CoinType) -> Single<Nonce>
  func getBinanceAccountInfo(userId: Int, type: CoinType) -> Single<BinanceAccountInfo>
  func getRippleSequence(userId: Int, type: CoinType) -> Single<RippleSequence>
  func getSellAddress(userId: Int, type: CoinType) -> Single<SellAddress>
  func getSellDetails(userId: Int, type: CoinType) -> Single<SellDetails>
  func getVerificationInfo(userId: Int) -> Single<VerificationInfo>
  func sendVerification(userId: Int, userData: VerificationUserData) -> Completable
  func sendVIPVerification(userId: Int, userData: VIPVerificationUserData) -> Completable
  func getPriceChartData(userId: Int, type: CoinType) -> Single<PriceChartData>
  func getBuyTrades(userId: Int, type: CoinType, page: Int) -> Single<BuySellTrades>
  func getSellTrades(userId: Int, type: CoinType, page: Int) -> Single<BuySellTrades>
  func updateLocation(userId: Int, latitude: Double, longitude: Double) -> Completable
}

final class APIGatewayImpl: APIGateway {
  let api: NetworkRequestExecutor
  
  required init(networkProvider api: NetworkRequestExecutor) {
    self.api = api
  }
  
  func createAccount(phoneNumber: String, password: String) -> Single<Account> {
    let request = CreateAccountRequest(phoneNumber: phoneNumber, password: password)
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
  
  func recoverWallet(phoneNumber: String, password: String) -> Single<Account> {
    let request = RecoverWalletRequest(phoneNumber: phoneNumber, password: password)
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
  
  func verifyCode(userId: Int, code: String) -> Completable {
    let request = VerifyCodeRequest(userId: userId, code: code)
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
  
  func addCoins(userId: Int, coinAddresses: [CoinAddress]) -> Completable {
    let request = AddCoinsRequest(userId: userId, coinAddresses: coinAddresses)
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
  
  func compareCoins(userId: Int, coinAddresses: [CoinAddress]) -> Completable {
    let request = CompareCoinsRequest(userId: userId, coinAddresses: coinAddresses)
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
  
  func getCoinsBalance(userId: Int, coins: [BTMCoin]) -> Single<CoinsBalance> {
    if coins.isEmpty {
      return Single.just(CoinsBalance(totalBalance: 0, coins: []))
    }
    
    let request = CoinsBalanceRequest(userId: userId, coins: coins)
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
  
  func getCoinSettings(type: CoinType) -> Single<CoinSettings> {
    let request = CoinSettingsRequest(coinId: type.code)
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
  
  func getMapAddresses() -> Single<MapAddresses> {
    let request = MapAddressesRequest()
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
  
  func getPhoneNumber(userId: Int) -> Single<PhoneNumber> {
    let request = GetPhoneNumberRequest(userId: userId)
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
  
  func checkPassword(userId: Int, password: String) -> Single<Bool> {
    let request = CheckPasswordRequest(userId: userId, password: password)
    return api.execute(request)
      .map { apiResponse -> Bool in
        switch apiResponse {
        case let .response(response):
          return response.result
        case let .error(error):
          throw error
        }
      }
  }
  
  func changePhone(userId: Int, phoneNumber: String) -> Completable {
    let request = ChangePhoneRequest(userId: userId, phoneNumber: phoneNumber)
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
  
  func confirmPhone(userId: Int, phoneNumber: String, code: String) -> Completable {
    let request = ConfirmPhoneRequest(userId: userId, phoneNumber: phoneNumber, code: code)
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
  
  func changePassword(userId: Int, oldPassword: String, newPassword: String) -> Completable {
    let request = ChangePasswordRequest(userId: userId, oldPassword: oldPassword, newPassword: newPassword)
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
  
  func unlink(userId: Int) -> Completable {
    let request = UnlinkRequest(userId: userId)
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
  
  func getTransactions(userId: Int, type: CoinType, page: Int) -> Single<Transactions> {
    let index = page * 10 + 1
    let request = TransactionsRequest(userId: userId, coinId: type.code, index: index)
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
  
  func getTransactionDetails(userId: Int, type: CoinType, id: String) -> Single<TransactionDetails> {
    let request = TransactionDetailsRequest(userId: userId, coinId: type.code, id: id)
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
  
  func getUtxos(userId: Int, type: CoinType, xpub: String) -> Single<[Utxo]> {
    let request = UtxosRequest(userId: userId, coinId: type.code, xpub: xpub)
    return api.execute(request)
      .flatMap {
        switch $0 {
        case let .response(response):
          return Single.just(response.utxos)
        case let .error(error):
          return Single.error(error)
        }
      }
  }
  
  func presubmitTransaction(userId: Int,
                            type: CoinType,
                            coinAmount: Double,
                            currencyAmount: Double) -> Single<PreSubmitResponse> {
    let request = PreSubmitTransactionRequest(userId: userId,
                                              coinId: type.code,
                                              coinAmount: coinAmount,
                                              currencyAmount: currencyAmount)
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
  
  func submitTransaction(userId: Int,
                         type: CoinType,
                         txType: TransactionType,
                         amount: Double,
                         phone: String?,
                         message: String?,
                         imageId: String?,
                         toCoinType: CoinType?,
                         txhex: String?) -> Completable {
    let request = SubmitTransactionRequest(userId: userId,
                                           coinId: type.code,
                                           txType: txType,
                                           amount: amount,
                                           phone: phone,
                                           message: message,
                                           imageId: imageId,
                                           toCoinId: toCoinType?.code,
                                           txhex: txhex)
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
  
  func requestCode(userId: Int) -> Completable {
    let request = RequestCodeRequest(userId: userId)
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
  
  func getTronBlockHeader(userId: Int, type: CoinType) -> Single<BTMTronBlockHeader> {
    let request = GetTronBlockHeaderRequest(userId: userId, coinId: type.code)
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
  
  func getGiftAddress(userId: Int, type: CoinType, phone: String) -> Single<GiftAddress> {
    let request = GetGiftAddressRequest(userId: userId, coinId: type.code, phone: phone)
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
  
  func getNonce(userId: Int, type: CoinType) -> Single<Nonce> {
    let request = GetNonceRequest(userId: userId, coinId: type.code)
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
  
  func getBinanceAccountInfo(userId: Int, type: CoinType) -> Single<BinanceAccountInfo> {
    let request = GetBinanceAccountInfoRequest(userId: userId, coinId: type.code)
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
  
  func getRippleSequence(userId: Int, type: CoinType) -> Single<RippleSequence> {
    let request = GetRippleSequenceRequest(userId: userId, coinId: type.code)
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
  
  func getSellAddress(userId: Int, type: CoinType) -> Single<SellAddress> {
    let request = GetSellAddressRequest(userId: userId, coinId: type.code)
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
  
  func getSellDetails(userId: Int, type: CoinType) -> Single<SellDetails> {
    let request = GetSellDetailsRequest(userId: userId, coinId: type.code)
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
  
  func getVerificationInfo(userId: Int) -> Single<VerificationInfo> {
    let request = GetVerificationInfoRequest(userId: userId)
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
  
  func sendVerification(userId: Int, userData: VerificationUserData) -> Completable {
    let request = SendVerificationRequest(userId: userId, userData: userData)
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
  
  func sendVIPVerification(userId: Int, userData: VIPVerificationUserData) -> Completable {
    let request = SendVIPVerificationRequest(userId: userId, userData: userData)
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
  
  func getPriceChartData(userId: Int, type: CoinType) -> Single<PriceChartData> {
    let request = GetPriceChartDataRequest(userId: userId, coinId: type.code)
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
  
  func getBuyTrades(userId: Int, type: CoinType, page: Int) -> Single<BuySellTrades> {
    let index = page * 10 + 1
    let request = BuySellTradesRequest(userId: userId, coinId: type.code, type: TradeType.buy, index: index)
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
  
  func getSellTrades(userId: Int, type: CoinType, page: Int) -> Single<BuySellTrades> {
    let index = page * 10 + 1
    let request = BuySellTradesRequest(userId: userId, coinId: type.code, type: TradeType.sell, index: index)
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
  
  func updateLocation(userId: Int, latitude: Double, longitude: Double) -> Completable {
    let request = UpdateLocationRequest(userId: userId, latitude: latitude, longitude: longitude)
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
  
}


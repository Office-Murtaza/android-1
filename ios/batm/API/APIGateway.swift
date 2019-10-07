import Foundation
import TrustWalletCore
import RxSwift

enum APIError: Error, Equatable {
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
  func getMapAddresses() -> Single<MapAddresses>
  func getPhoneNumber(userId: Int) -> Single<PhoneNumber>
  func checkPassword(userId: Int, password: String) -> Single<Bool>
  func changePhone(userId: Int, phoneNumber: String) -> Completable
  func confirmPhone(userId: Int, phoneNumber: String, code: String) -> Completable
  func changePassword(userId: Int, oldPassword: String, newPassword: String) -> Completable
  func unlink(userId: Int) -> Completable
  func getTransactions(userId: Int, type: CoinType, page: Int) -> Single<Transactions>
  func getUtxos(userId: Int, type: CoinType, xpub: String) -> Single<[Utxo]>
  func submitTransaction(userId: Int,
                         type: CoinType,
                         txType: TransactionType,
                         amount: Double,
                         phone: String?,
                         message: String?,
                         imageUrl: String?,
                         txhex: String?,
                         trxJson: [String: Any]?) -> Completable
  func requestCode(userId: Int) -> Completable
  func getTronBlockHeader(userId: Int, type: CoinType) -> Single<BTMTronBlockHeader>
  func getGiftAddress(userId: Int, type: CoinType, phone: String) -> Single<GiftAddress>
  func getNonce(userId: Int, type: CoinType, address: String) -> Single<Nonce>
  func getBinanceAccountInfo(userId: Int, type: CoinType, address: String) -> Single<BinanceAccountInfo>
  func getRippleSequence(userId: Int, type: CoinType, address: String) -> Single<RippleSequence>
}

final class APIGatewayImpl: APIGateway {
  let api: NetworkRequestExecutor
  let tron: NetworkRequestExecutor
  
  required init(networkProvider api: NetworkRequestExecutor,
                tronAPIProvider tron: NetworkRequestExecutor) {
    self.api = api
    self.tron = tron
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
          return response.matched
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
  
  func submitTransaction(userId: Int,
                         type: CoinType,
                         txType: TransactionType,
                         amount: Double,
                         phone: String?,
                         message: String?,
                         imageUrl: String?,
                         txhex: String?,
                         trxJson: [String: Any]?) -> Completable {
    let request = SubmitTransactionRequest(userId: userId,
                                           coinId: type.code,
                                           txType: txType,
                                           amount: amount,
                                           phone: phone,
                                           message: message,
                                           imageUrl: imageUrl,
                                           txhex: txhex,
                                           trxJson: trxJson)
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
  
  func getNonce(userId: Int, type: CoinType, address: String) -> Single<Nonce> {
    let request = GetNonceRequest(userId: userId, coinId: type.code, address: address)
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
  
  func getBinanceAccountInfo(userId: Int, type: CoinType, address: String) -> Single<BinanceAccountInfo> {
    let request = GetBinanceAccountInfoRequest(userId: userId, coinId: type.code, address: address)
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
  
  func getRippleSequence(userId: Int, type: CoinType, address: String) -> Single<RippleSequence> {
    let request = GetRippleSequenceRequest(userId: userId, coinId: type.code, address: address)
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
  
}


import Foundation
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
  func getCoinsBalance(userId: Int) -> Single<CoinsBalance>
  func getMapAddresses() -> Single<MapAddresses>
  
}

final class APIGatewayImpl: APIGateway {
  let api: NetworkRequestExecutor
  
  required init(networkProvider apiProvider: NetworkRequestExecutor) {
    self.api = apiProvider
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
  
  func getCoinsBalance(userId: Int) -> Single<CoinsBalance> {
    let request = CoinsBalanceRequest(userId: userId)
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
  
}


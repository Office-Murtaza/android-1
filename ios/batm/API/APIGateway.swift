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
  func verifyCode(userId: Int, code: String) -> Completable
  func addCoins(userId: Int, coins: [CoinAddress]) -> Completable
  
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
  
  func addCoins(userId: Int, coins: [CoinAddress]) -> Completable {
    let request = AddCoinsRequest(userId: userId, coins: coins)
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


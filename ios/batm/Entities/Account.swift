import Foundation

protocol AccountProtocol {
    var userId: String { get }
    var accessToken: String { get }
    var refreshToken: String { get }
    var expires: Date { get }
}

struct Account: Equatable, AccountProtocol {
  let userId: String
  let accessToken: String
  let refreshToken: String
  let expires: Date
}

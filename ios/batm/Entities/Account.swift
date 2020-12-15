import Foundation

protocol AccountProtocol {
    var userId: Int { get }
    var accessToken: String { get }
    var refreshToken: String { get }
    var expires: Date { get }
}

struct Account: Equatable, AccountProtocol {
  let userId: Int
  let accessToken: String
  let refreshToken: String
  let expires: Date
}

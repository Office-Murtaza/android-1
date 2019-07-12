import Foundation

struct Account: Equatable {
  let userId: Int
  let accessToken: String
  let refreshToken: String
  let expires: Date
}

import Foundation

struct CheckAccountResponse: Equatable {
  let phoneExist: Bool
  let passwordMatch: Bool
}

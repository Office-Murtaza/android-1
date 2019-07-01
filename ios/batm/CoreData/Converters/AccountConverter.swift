import Foundation
import CoreData

class AccountConverter: Converter<AccountRecord, Account> {
  override func convert(model: AccountRecord) throws -> Account {
    return Account(userId: Int(model.userId),
                   accessToken: model.accessToken,
                   refreshToken: model.refreshToken,
                   expires: model.expires)
  }
}

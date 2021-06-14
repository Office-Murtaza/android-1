import Foundation
import CoreData

class AccountConverter: Converter<AccountRecord, Account> {
  override func convert(model: AccountRecord) throws -> Account {
    return Account(userId: model.userId,
                   accessToken: model.accessToken,
                   refreshToken: model.refreshToken,
                   expires: model.expires)
  }
}

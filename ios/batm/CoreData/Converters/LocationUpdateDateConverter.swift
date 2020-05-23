import Foundation
import CoreData
import TrustWalletCore

class LocationUpdateDateConverter: Converter<LocationUpdateDateRecord, Date> {
  override func convert(model: LocationUpdateDateRecord) throws -> Date {
    return model.updateDate
  }
}

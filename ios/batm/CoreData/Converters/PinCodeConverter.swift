import Foundation
import CoreData

class PinCodeConverter: Converter<PinCodeRecord, String> {
  override func convert(model: PinCodeRecord) throws -> String {
    return model.pinCode
  }
}

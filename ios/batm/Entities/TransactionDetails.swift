import UIKit

struct TransactionDetails: Equatable {
  let txId: String?
  let txDbId: String?
  let link: String?
  let type: TransactionType
  let status: TransactionStatus
  let cashStatus: TransactionCashStatus?
  let fiatAmount: Double?
  let cryptoAmount: Double?
  let cryptoFee: Double?
  let dateString: String?
  let fromAddress: String?
  let toAddress: String?
  let phone: String?
  let imageId: String?
  let message: String?
  let sellInfo: String?
}

extension TransactionDetails {
  
  var hasGiftInfo: Bool {
    return phone != nil || imageId != nil || message != nil
  }
  
  var hasSellInfo: Bool {
    return sellInfo != nil
  }
  
}

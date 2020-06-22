import UIKit
import TrustWalletCore

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
  let refTxId: String?
  let refLink: String?
  let refCoin: CustomCoinType?
  let refCryptoAmount: Double?
  let sellInfo: String?
}

extension TransactionDetails {
  
  var hasGiftInfo: Bool {
    return phone != nil || imageId != nil || message != nil
  }
  
  var hasExchangeInfo: Bool {
    return refTxId != nil || refCoin != nil || refCryptoAmount != nil
  }
  
  var hasSellInfo: Bool {
    return sellInfo != nil
  }
  
}

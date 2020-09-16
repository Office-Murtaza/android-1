import UIKit
import TrustWalletCore

struct TransactionDetails: Equatable {
  let txId: String?
  let txDbId: String?
  let link: String?
  let type: TransactionType
  let status: TransactionStatus
  let cashStatus: TransactionCashStatus?
  let cryptoAmount: Decimal?
  let fiatAmount: Decimal?
  let cryptoFee: Decimal?
  let fiatFee: Decimal?
  let dateString: String?
  let fromAddress: String?
  let toAddress: String?
  let fromPhone: String?
  let toPhone: String?
  let imageId: String?
  let message: String?
  let refTxId: String?
  let refLink: String?
  let refCoin: CustomCoinType?
  let refCryptoAmount: Decimal?
  let sellInfo: String?
}

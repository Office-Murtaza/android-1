import UIKit
import TrustWalletCore

struct TransactionDetails: Equatable {
  let txId: String?
  let txDbId: String?
  let link: String?
  let type: TransactionType
  let status: TransactionStatus
  let cashStatus: TransactionCashStatus?
  let cryptoAmount: Double?
  let fiatAmount: Double?
  let cryptoFee: Double?
  let fiatFee: Double?
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

import UIKit
import TrustWalletCore

struct TransactionDetails: Equatable {
    let txId: String?
    let txDbId: Int?
    let link: String?
    let type: TransactionType?
    let status: TransactionStatus?
    let confirmations: Int?
    let cryptoAmount: Double?
    let cryptoFee: Double?
    let fromAddress: String?
    let toAddress: String?
    let fromPhone: String?
    let toPhone: String?
    let imageId: String?
    let message: String?
    let swapTxId: String?
    let swapLink: String?
    let swapCoin: CustomCoinType?
    let swapCryptoAmount: Double?
    let fiatAmount: Double?
    let cashStatus: TransactionCashStatus?
    let sellInfo: String?
    let date: String
}

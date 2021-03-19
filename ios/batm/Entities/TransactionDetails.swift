import UIKit
import TrustWalletCore

struct TransactionDetails: Equatable {
    let txId: String?
    let txDbId: Int?
    let link: String?
    let coin: CustomCoinType?
    let userId: Int?
    let type: TransactionType?
    let status: TransactionStatus?
    let confirmations: Int?
    let cryptoAmount: Double?
    let cryptoFee: Double?
    let fromAddress: String?
    let toAddress: String?
    let fromPhone: String?
    let toPhone: String?
    let image: String?
    let message: String?
    let refTxId: String?
    let refLink: String?
    let refCoin: CustomCoinType?
    let refCryptoAmount: Double?
    let fiatAmount: Double?
    let cashStatus: TransactionCashStatus?
    let sellInfo: String?
    let processed: Int?
    let timestamp: Int?
}

extension TransactionDetails {
    static var empty: TransactionDetails = TransactionDetails(txId: nil,
                                                              txDbId: nil,
                                                              link: nil,
                                                              coin: nil,
                                                              userId: nil,
                                                              type: nil,
                                                              status: nil,
                                                              confirmations: nil,
                                                              cryptoAmount: nil,
                                                              cryptoFee: nil,
                                                              fromAddress: nil,
                                                              toAddress: nil,
                                                              fromPhone: nil,
                                                              toPhone: nil,
                                                              image: nil,
                                                              message: nil,
                                                              refTxId: nil,
                                                              refLink: nil,
                                                              refCoin: nil,
                                                              refCryptoAmount: nil,
                                                              fiatAmount: nil,
                                                              cashStatus: nil,
                                                              sellInfo: nil,
                                                              processed: nil,
                                                              timestamp: nil)
}

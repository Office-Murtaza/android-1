import Foundation
import TrustWalletCore

protocol TransactionDetailsModule: AnyObject {
    func setup(with transactionDetails: TransactionDetails, coinType: CustomCoinType)
}

protocol TransactionDetailsModuleDelegate: AnyObject {}

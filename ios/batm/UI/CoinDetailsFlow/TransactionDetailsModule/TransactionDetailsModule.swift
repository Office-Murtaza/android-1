import Foundation
import TrustWalletCore

protocol TransactionDetailsModule: AnyObject {
    func setup(with details: TransactionDetails, coinType: CustomCoinType)
}
protocol TransactionDetailsModuleDelegate: AnyObject {}

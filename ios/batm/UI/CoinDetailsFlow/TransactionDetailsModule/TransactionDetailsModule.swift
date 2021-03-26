import Foundation
import TrustWalletCore

protocol TransactionDetailsModule: AnyObject {
    func setup(with details: TransactionDetails)
}
protocol TransactionDetailsModuleDelegate: AnyObject {}

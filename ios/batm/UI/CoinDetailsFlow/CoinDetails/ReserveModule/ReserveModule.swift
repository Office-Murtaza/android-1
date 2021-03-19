import Foundation

protocol ReserveModule: AnyObject {
    func setup(with coinType: CustomCoinType)
}
protocol ReserveModuleDelegate: AnyObject {
    func didFinishReserve(with transactionResult: String, transactionDetails: TransactionDetails?)
}

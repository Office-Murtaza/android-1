import Foundation

protocol RecallModule: AnyObject {
    func setup(with coinType: CustomCoinType)
}
protocol RecallModuleDelegate: AnyObject {
    func didFinishRecall(with transactionResult: String, transactionDetails: TransactionDetails?)
}

import Foundation

protocol CoinWithdrawModule: AnyObject {
    func setup(with coinType: CustomCoinType)
}
protocol CoinWithdrawModuleDelegate: AnyObject {
    func didFinishCoinWithdraw(with transactionResult: String, transactionDetails: TransactionDetails?)
}

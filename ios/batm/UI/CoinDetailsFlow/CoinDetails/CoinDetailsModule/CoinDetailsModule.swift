import Foundation
import TrustWalletCore

protocol CoinDetailsModule: AnyObject {
    func setup(with type: CustomCoinType)
    func setup(transactionDetails: TransactionDetails?)
    func setup(predefinedData: CoinDetailsPredefinedDataConfig)
}
protocol CoinDetailsModuleDelegate: AnyObject {
    func showDepositScreen(with coinType: CustomCoinType)
    func showWithdrawScreen(with coinType: CustomCoinType)
    func showTransactionDetails(with transactionDetails: TransactionDetails, coinType: CustomCoinType)
    func showReserve(with coinType: CustomCoinType)
    func showRecall(with coinType: CustomCoinType)
}

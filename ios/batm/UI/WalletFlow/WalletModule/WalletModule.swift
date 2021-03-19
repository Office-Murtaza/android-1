import Foundation

protocol WalletModule: AnyObject {
    func fetchCoinsBalance()
}
protocol WalletModuleDelegate: AnyObject {
    func showCoinDetails(for type: CustomCoinType)
    func showCoinDetail(predefinedConfig: CoinDetailsPredefinedDataConfig)
}

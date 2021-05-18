import Foundation

protocol CoinDepositModule: AnyObject {
  func setup(with coinType: CustomCoinType)
}
protocol CoinDepositModuleDelegate: AnyObject {}

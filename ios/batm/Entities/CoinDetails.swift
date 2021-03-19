import Foundation
import TrustWalletCore

struct CoinDetails: Equatable {
    var byteFee: Int?
    var txFee: Decimal?
    var txFeeStr: String?
    var convertedTxFee: Decimal?
    var convertedTxFeeStr: String?
    var scale: Int?
    var gasPrice: Int?
    var gasLimit: Int?
    var platformSwapFee: Decimal?
    var platformSwapFeeStr: String?
    var platformTradeFee: Decimal?
    var platformTradeFeeStr: String?
    var walletAddress: String?
    var contractAddress: String?
}

extension CoinDetails {
    static var empty: CoinDetails {
        return CoinDetails(byteFee: nil,
                           txFee: nil,
                           txFeeStr: nil,
                           convertedTxFee: nil,
                           convertedTxFeeStr: nil,
                           scale: nil,
                           gasPrice: nil,
                           gasLimit: nil,
                           platformSwapFee: nil,
                           platformSwapFeeStr: nil,
                           platformTradeFee: nil,
                           platformTradeFeeStr: nil,
                           walletAddress: nil,
                           contractAddress: nil)
    }
}

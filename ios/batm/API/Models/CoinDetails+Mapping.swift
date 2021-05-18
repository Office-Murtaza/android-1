import ObjectMapper
import TrustWalletCore

extension CoinDetails: ImmutableMappable {
    init(map: Map) throws {
        byteFee = try? map.value("")
        txFee = Decimal(string: (try? map.value("txFeeStr")) ?? "")
        convertedTxFee = Decimal(string: (try? map.value("convertedTxFeeStr")) ?? "")
        scale = try? map.value("scale")
        gasPrice = try? map.value("gasPrice")
        gasLimit = try? map.value("gasLimit")
        platformSwapFee = Decimal(string: (try? map.value("platformSwapFeeStr")) ?? "")
        platformTradeFee = Decimal(string: (try? map.value("platformTradeFeeStr")) ?? "")
        walletAddress = try? map.value("walletAddress")
        contractAddress = try? map.value("contractAddress")
    }
}

import Foundation
import TrustWalletCore

extension CoinType {
  
  var code: String {
    switch self {
    case .bitcoin: return "BTC"
    case .ethereum: return "ETH"
    case .bitcoinCash: return "BCH"
    case .litecoin: return "LTC"
    case .binance: return "BNB"
    case .tron: return "TRX"
    case .ripple: return "XRP"
    default: return ""
    }
  }
  
}

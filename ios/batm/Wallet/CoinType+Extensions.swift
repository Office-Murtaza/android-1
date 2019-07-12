import Foundation
import TrustWalletCore

extension CoinType {
  
  var verboseValue: String {
    switch self {
    case .bitcoin: return "Bitcoin"
    case .ethereum: return "Ethereum"
    case .bitcoinCash: return "Bitcoin Cash"
    case .litecoin: return "Litecoin"
    case .binance: return "Binance"
    case .tron: return "Tron"
    case .ripple: return "XRP"
    default: return ""
    }
  }
  
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
  
  init?(code: String) {
    switch code {
    case CoinType.bitcoin.code: self = .bitcoin
    case CoinType.ethereum.code: self = .ethereum
    case CoinType.bitcoinCash.code: self = .bitcoinCash
    case CoinType.litecoin.code: self = .litecoin
    case CoinType.binance.code: self = .binance
    case CoinType.tron.code: self = .tron
    case CoinType.ripple.code: self = .ripple
    default: return nil
    }
  }
  
}

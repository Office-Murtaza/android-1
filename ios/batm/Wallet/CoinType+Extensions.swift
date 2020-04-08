import Foundation
import TrustWalletCore

extension CoinType {
  
  static let maxNumberOfFractionDigits = 6
  
  var verboseValue: String {
    switch self {
    case .bitcoin: return "Bitcoin"
    case .ethereum: return "Ethereum"
    case .bitcoinCash: return "Bitcoin Cash"
    case .litecoin: return "Litecoin"
    case .binance: return "Binance"
    case .tron: return "Tron"
    case .xrp: return "Ripple"
    default: return ""
    }
  }
  
  var logo: UIImage? {
    switch self {
    case .bitcoin: return UIImage(named: "coins_bitcoin")
    case .ethereum: return UIImage(named: "coins_ethereum")
    case .bitcoinCash: return UIImage(named: "coins_bitcoin_cash")
    case .litecoin: return UIImage(named: "coins_litecoin")
    case .binance: return UIImage(named: "coins_binance")
    case .tron: return UIImage(named: "coins_tron")
    case .xrp: return UIImage(named: "coins_ripple")
    default: return nil
    }
  }
  
  var smallLogo: UIImage? {
    switch self {
    case .bitcoin: return UIImage(named: "coins_bitcoin_small")
    case .ethereum: return UIImage(named: "coins_ethereum_small")
    case .bitcoinCash: return UIImage(named: "coins_bitcoin_cash_small")
    case .litecoin: return UIImage(named: "coins_litecoin_small")
    case .binance: return UIImage(named: "coins_binance_small")
    case .tron: return UIImage(named: "coins_tron_small")
    case .xrp: return UIImage(named: "coins_ripple_small")
    default: return nil
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
    case .xrp: return "XRP"
    default: return ""
    }
  }
  
  var unit: Int64 {
    switch self {
    case .bitcoin, .bitcoinCash, .litecoin, .binance: return 100_000_000
    case .ethereum: return 1_000_000_000_000_000_000
    case .tron, .xrp: return 1_000_000
    default: return 0
    }
  }
  
  var hashType: UInt32 {
    switch self {
    case .bitcoinCash: return BitcoinSigHashType.fork.rawValue | BitcoinSigHashType.all.rawValue
    default: return BitcoinSigHashType.all.rawValue
    }
  }
  
  var customPurpose: Purpose {
    switch self {
    case .bitcoin: return .bip44
    default: return purpose
    }
  }
  
  var customVersion: HDVersion {
    switch self {
    case .bitcoin: return .xpub
    default: return xpubVersion
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
    case CoinType.xrp.code: self = .xrp
    default: return nil
    }
  }
  
}

import Foundation
import TrustWalletCore

enum CustomCoinType: CaseIterable {
  
  case bitcoin
  case bitcoinCash
  case litecoin
  case ethereum
  case catm
  case binance
  case tron
  case ripple
  
  static let maxNumberOfFractionDigits = 6
  
  var verboseValue: String {
    switch self {
    case .bitcoin: return "Bitcoin"
    case .bitcoinCash: return "Bitcoin Cash"
    case .litecoin: return "Litecoin"
    case .ethereum: return "Ethereum"
    case .catm: return "CATM"
    case .binance: return "Binance"
    case .tron: return "Tron"
    case .ripple: return "Ripple"
    }
  }
  
  var logo: UIImage? {
    switch self {
    case .bitcoin: return UIImage(named: "coins_bitcoin")
    case .bitcoinCash: return UIImage(named: "coins_bitcoin_cash")
    case .litecoin: return UIImage(named: "coins_litecoin")
    case .ethereum: return UIImage(named: "coins_ethereum")
    case .catm: return UIImage(named: "coins_catm")
    case .binance: return UIImage(named: "coins_binance")
    case .tron: return UIImage(named: "coins_tron")
    case .ripple: return UIImage(named: "coins_ripple")
    }
  }
  
  var smallLogo: UIImage? {
    switch self {
    case .bitcoin: return UIImage(named: "coins_bitcoin_small")
    case .bitcoinCash: return UIImage(named: "coins_bitcoin_cash_small")
    case .litecoin: return UIImage(named: "coins_litecoin_small")
    case .ethereum: return UIImage(named: "coins_ethereum_small")
    case .catm: return UIImage(named: "coins_catm_small")
    case .binance: return UIImage(named: "coins_binance_small")
    case .tron: return UIImage(named: "coins_tron_small")
    case .ripple: return UIImage(named: "coins_ripple_small")
    }
  }
  
  var code: String {
    switch self {
    case .bitcoin: return "BTC"
    case .bitcoinCash: return "BCH"
    case .litecoin: return "LTC"
    case .ethereum: return "ETH"
    case .catm: return "CATM"
    case .binance: return "BNB"
    case .tron: return "TRX"
    case .ripple: return "XRP"
    }
  }
  
  var defaultCoinType: CoinType {
    switch self {
    case .bitcoin: return .bitcoin
    case .bitcoinCash: return .bitcoinCash
    case .litecoin: return .litecoin
    case .ethereum, .catm: return .ethereum
    case .binance: return .binance
    case .tron: return .tron
    case .ripple: return .xrp
    }
  }
  
  var unit: Int64 {
    switch self {
    case .bitcoin, .bitcoinCash, .litecoin, .binance: return 100_000_000
    case .ethereum, .catm: return 1_000_000_000_000_000_000
    case .tron, .ripple: return 1_000_000
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
    default: return defaultCoinType.purpose
    }
  }
  
  var customVersion: HDVersion {
    switch self {
    case .bitcoin: return .xpub
    default: return defaultCoinType.xpubVersion
    }
  }
  
  init?(code: String) {
    switch code {
    case CustomCoinType.bitcoin.code: self = .bitcoin
    case CustomCoinType.bitcoinCash.code: self = .bitcoinCash
    case CustomCoinType.litecoin.code: self = .litecoin
    case CustomCoinType.ethereum.code: self = .ethereum
    case CustomCoinType.catm.code: self = .catm
    case CustomCoinType.binance.code: self = .binance
    case CustomCoinType.tron.code: self = .tron
    case CustomCoinType.ripple.code: self = .ripple
    default: return nil
    }
  }
  
}

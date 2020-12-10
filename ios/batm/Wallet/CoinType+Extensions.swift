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
  case usdt
  case dash
  case doge
  
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
    case .usdt: return "Tether"
    case .dash: return "Dash"
    case .doge: return "Dogecoin"
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
    case .usdt: return UIImage(named: "coins_usdt")
    case .dash: return UIImage(named: "coins_dash")
    case .doge: return UIImage(named: "coins_doge")
    }
  }
  
  var mediumLogo: UIImage? {
    switch self {
    case .bitcoin: return UIImage(named: "coins_bitcoin_medium")
    case .bitcoinCash: return UIImage(named: "coins_bitcoin_cash_medium")
    case .litecoin: return UIImage(named: "coins_litecoin_medium")
    case .ethereum: return UIImage(named: "coins_ethereum_medium")
    case .catm: return UIImage(named: "coins_catm_medium")
    case .binance: return UIImage(named: "coins_binance_medium")
    case .tron: return UIImage(named: "coins_tron_medium")
    case .ripple: return UIImage(named: "coins_ripple_medium")
    case .usdt: return UIImage(named: "coins_usdt_medium")
    case .dash: return UIImage(named: "coins_dash_medium")
    case .doge: return UIImage(named: "coins_doge_medium")
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
    case .usdt: return UIImage(named: "coins_usdt_small")
    case .dash: return UIImage(named: "coins_dash_small")
    case .doge: return UIImage(named: "coins_doge_small")
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
    case .usdt: return "USDT"
    case .dash: return "DASH"
    case .doge: return "DOGE"
    }
  }
  
  var defaultCoinType: CoinType {
    switch self {
    case .bitcoin: return .bitcoin
    case .bitcoinCash: return .bitcoinCash
    case .litecoin: return .litecoin
    case .ethereum, .catm, .usdt: return .ethereum
    case .binance: return .binance
    case .tron: return .tron
    case .ripple: return .xrp
    case .dash: return .dash
    case .doge: return .dogecoin
    }
  }
  
  var unit: Int64 {
    switch self {
    case .bitcoin, .bitcoinCash, .litecoin, .binance, .dash, .doge: return 100_000_000
    case .ethereum, .catm, .usdt: return 1_000_000_000_000_000_000
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
    
    var isETHBased: Bool {
        switch self {
        case .catm, .usdt: return true
        default: return false
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
    case CustomCoinType.usdt.code: self = .usdt
    case CustomCoinType.dash.code: self = .dash
    case CustomCoinType.doge.code: self = .doge
    default: return nil
    }
  }
}

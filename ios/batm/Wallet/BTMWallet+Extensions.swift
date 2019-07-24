import Foundation
import TrustWalletCore

extension BTMWallet {
  
  static let supportedCoins: [CoinType] = [.bitcoin,
                                           .ethereum,
                                           .bitcoinCash,
                                           .litecoin,
                                           .binance,
                                           .tron,
                                           .ripple]
  
  init(hdWallet: HDWallet) {
    let seedPhrase = hdWallet.mnemonic
    
    let coins: [BTMCoin] = BTMWallet.supportedCoins.map {
      let privateKey = hdWallet.getKeyForCoin(coin: $0)
      let publicKey: String
      
      switch $0 {
      case .bitcoin:
        let bitcoinPublicKey = privateKey.getPublicKeySecp256k1(compressed: true)
        publicKey = BitcoinAddress(publicKey: bitcoinPublicKey,
                                   prefix: P2PKHPrefix.bitcoin.rawValue).description
      default:
        publicKey = $0.deriveAddress(privateKey: privateKey)
      }
      
      return BTMCoin(type: $0,
                     privateKey: privateKey.data.hexString,
                     publicKey: publicKey,
                     isVisible: true)
    }
    
    self.init(seedPhrase: seedPhrase, coins: coins)
  }
  
}

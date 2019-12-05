import Foundation
import TrustWalletCore

extension BTMWallet {
  
  static let supportedCoins: [CoinType] = [.bitcoin,
                                           .ethereum,
                                           .bitcoinCash,
                                           .litecoin,
                                           .binance,
                                           .tron,
                                           .xrp]
  
  init(hdWallet: HDWallet) {
    let seedPhrase = hdWallet.mnemonic
    
    let coins: [BTMCoin] = BTMWallet.supportedCoins.map {
      let privateKey = hdWallet.getKeyForCoin(coin: $0)
      let publicKey: String
      
      switch $0 {
      case .bitcoin:
        let extPubKey = hdWallet.getExtendedPubKey(purpose: .bip44, coin: $0, version: .xpub)
        let path = DerivationPath(purpose: .bip44, coinType: $0).description
        let bitcoinPublicKey = HDWallet.getPublicKeyFromExtended(extended: extPubKey, derivationPath: path)!
        publicKey = BitcoinAddress(publicKey: bitcoinPublicKey, prefix: $0.p2pkhPrefix)!.description
      default:
        publicKey = $0.deriveAddress(privateKey: privateKey)
      }
      
      return BTMCoin(type: $0,
                     privateKey: privateKey.data.hexString,
                     publicKey: publicKey)
    }
    
    self.init(seedPhrase: seedPhrase, coins: coins)
  }
  
}

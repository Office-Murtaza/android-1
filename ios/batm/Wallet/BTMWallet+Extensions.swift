import Foundation
import TrustWalletCore

extension BTMWallet {
  
  init(hdWallet: HDWallet) {
    let seedPhrase = hdWallet.mnemonic
    
    let coins: [BTMCoin] = CustomCoinType.allCases.map {
      let coinType = $0.defaultCoinType
      let privateKey = hdWallet.getKeyForCoin(coin: coinType)
      let address: String
      
      switch $0 {
      case .bitcoin:
        let extPubKey = hdWallet.getExtendedPublicKey(purpose: .bip44, coin: coinType, version: .xpub)
        let path = DerivationPath(purpose: .bip44, coinType: coinType).description
        let publicKey = HDWallet.getPublicKeyFromExtended(extended: extPubKey, derivationPath: path)!
        address = BitcoinAddress(publicKey: publicKey, prefix: coinType.p2pkhPrefix)!.description
      default:
        address = coinType.deriveAddress(privateKey: privateKey)
      }
      
      return BTMCoin(type: $0,
                     privateKey: privateKey.data.hexString,
                     address: address)
    }
    
    self.init(seedPhrase: seedPhrase, coins: coins)
  }
  
}

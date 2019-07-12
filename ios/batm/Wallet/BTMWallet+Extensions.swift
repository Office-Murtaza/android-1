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
    
    print("-------------------------------------------------")
    print("SEED_PHRASE:", seedPhrase)
    
    let coinAddresses: [CoinAddress] = BTMWallet.supportedCoins.map {
      let privateKey = hdWallet.getKeyForCoin(coin: $0)
      let address: String
      
      switch $0 {
      case .bitcoin:
        let publicKey = privateKey.getPublicKeySecp256k1(compressed: true)
        address = BitcoinAddress(publicKey: publicKey, prefix: P2PKHPrefix.bitcoin.rawValue).description
      default:
        address = $0.deriveAddress(privateKey: privateKey)
      }
      
      print("***********")
      print("COIN_TYPE:", $0.code)
      print("PRIVATE_KEY:", privateKey.data.hexString)
      print("PUBLIC_KEY:", address)
      print("***********")
      
      return CoinAddress(type: $0, address: address)
    }
    
    print("-------------------------------------------------")
    
    self.init(seedPhrase: seedPhrase, coinAddresses: coinAddresses)
  }
  
}

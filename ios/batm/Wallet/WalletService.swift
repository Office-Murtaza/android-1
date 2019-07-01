import Foundation
import RxSwift
import TrustWalletCore

protocol WalletService {
  func createWallet() -> Completable
}

class WalletServiceImpl: WalletService {
  
  let supportedCoins: [CoinType] = [.bitcoin,
                                    .ethereum,
                                    .bitcoinCash,
                                    .litecoin,
                                    .binance,
                                    .tron,
                                    .ripple]
  
  let walletStorage: BTMWalletStorage
  
  init(walletStorage: BTMWalletStorage) {
    self.walletStorage = walletStorage
  }

  func createWallet() -> Completable {
    let hdWallet = HDWallet(strength: 160, passphrase: "")
    let seedPhrase = hdWallet.mnemonic
    print("-------------------------------------------------")
    print("SEED_PHRASE:", seedPhrase)
    let coinAddresses: [CoinAddress] = supportedCoins.map {
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
    let btmWallet = BTMWallet(seedPhrase: seedPhrase,
                              coinAddresses: coinAddresses)
    
    return walletStorage.save(wallet: btmWallet)
  }
  
}

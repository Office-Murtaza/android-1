import Foundation
import RxSwift
import TrustWalletCore

protocol WalletService {
  func createWallet() -> Completable
  func recoverWallet(seedPhrase: String) -> Completable
}

class WalletServiceImpl: WalletService {
  
  let walletStorage: BTMWalletStorage
  
  init(walletStorage: BTMWalletStorage) {
    self.walletStorage = walletStorage
  }

  func createWallet() -> Completable {
    let hdWallet = HDWallet(strength: 128, passphrase: "")
    let btmWallet = BTMWallet(hdWallet: hdWallet)
    
    return walletStorage.save(wallet: btmWallet)
  }
  
  func recoverWallet(seedPhrase: String) -> Completable {
    let hdWallet = HDWallet(mnemonic: seedPhrase, passphrase: "")
    let btmWallet = BTMWallet(hdWallet: hdWallet)
    
    return walletStorage.save(wallet: btmWallet)
  }
  
}

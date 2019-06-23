import Foundation
import TrustWalletCore

extension HDWallet {
    static let test = HDWallet(mnemonic: "ripple scissors kick mammal hire column oak again sun offer wealth tomorrow wagon turn fatal",
                               passphrase: "TREZOR")
}

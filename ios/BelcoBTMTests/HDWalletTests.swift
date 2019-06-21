import XCTest
import TrustWalletCore

class HDWalletTests: XCTestCase {
    
    var wallet: HDWallet!

    override func setUp() {
        wallet = HDWallet.test
    }

    func test_deriveAddress_shouldDeriveCorrectAddressForBitcoin() {
        let bitcoin = CoinType.bitcoin
        let key = wallet.getKeyForCoin(coin: bitcoin)
        let address = bitcoin.deriveAddress(privateKey: key)
        
        XCTAssertEqual("bc1qumwjg8danv2vm29lp5swdux4r60ezptzz7ce85", address)
    }

}


import Foundation

protocol P2PCreateOrderPresenterInput: AnyObject {
    func updatedFiatAmount(fiat: String, fee: Double, price: Double)
}

protocol P2PCreateOrderPresenterOutput: AnyObject {
    func updated(crypto: String, fee: String)
}

class P2PCreateOrderPresenter: P2PCreateOrderPresenterInput {
    
    weak var output: P2PCreateOrderPresenterOutput?
    let model = P2PCreateOrderModel()
    
    func updatedFiatAmount(fiat: String, fee: Double, price: Double) {
        let data = model.calculateOrderData(fiatAmount: fiat.doubleValue ?? 0, platformFee: fee, tradePrice: price)
        output?.updated(crypto: data.cryptoAmount.formatted() ?? "0", fee: data.feeAmount.formatted() ?? "0")
    }
}

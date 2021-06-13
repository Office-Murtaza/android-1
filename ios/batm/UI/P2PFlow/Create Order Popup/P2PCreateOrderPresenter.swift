
import Foundation

protocol P2PCreateOrderPresenterInput: AnyObject {
  func updatedFiatAmount(trade: Trade, fiat: String, fee: Double, price: Double, reservedBalance: Double, type: TradeType)
}

protocol P2PCreateOrderPresenterOutput: AnyObject {
  func updated(crypto: String, fee: String, total: String, error: P2PCreateOrderValidationError?)
}

class P2PCreateOrderPresenter: P2PCreateOrderPresenterInput {
    
    weak var output: P2PCreateOrderPresenterOutput?
    let model: P2PCreateOrderModelInput = P2PCreateOrderModel()
    
  func updatedFiatAmount(trade: Trade, fiat: String, fee: Double, price: Double, reservedBalance: Double, type: TradeType) {
        let (data, error) = model.calculateOrderData(trade: trade,
                                              fiatAmount: fiat.doubleValue ?? 0,
                                              platformFee: fee,
                                              reservedBalance: reservedBalance,
                                              type: type)
        
      output?.updated(crypto: data.cryptoAmount.coinFormatted,
                      fee: data.feeAmount.coinFormatted,
                      total: data.totalReuslt.coinFormatted,
                      error: error)
    }
}

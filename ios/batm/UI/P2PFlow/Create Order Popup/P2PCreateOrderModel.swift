import Foundation

struct P2PCreateOrderData {
    let cryptoAmount: Double
    let feeAmount: Double
}

protocol P2PCreateOrderModelInput: AnyObject {
    func calculateOrderData(fiatAmount: Double, platformFee: Double, tradePrice: Double) -> P2PCreateOrderData
}

class P2PCreateOrderModel: P2PCreateOrderModelInput {
    
    func calculateOrderData(fiatAmount: Double, platformFee: Double, tradePrice: Double) -> P2PCreateOrderData {
        let cryptoAmount = calculateCryptoAmount(fiatAmount, tradePrice)
        let feeAmount = calculateFeeAmount(cryptoAmount, platformFee)
        
        return P2PCreateOrderData(cryptoAmount: cryptoAmount, feeAmount: feeAmount)
    }
    
    private func calculateCryptoAmount(_ fiatAmount: Double,_ tradePrice: Double) -> Double {
        let cryptoAmount = fiatAmount / tradePrice
        return cryptoAmount
    }
    
    private func calculateFeeAmount(_ cryptoAmount: Double,_ patformFee: Double) -> Double {
        let feeAmount = (cryptoAmount * patformFee) / 100
        return feeAmount
    }
}

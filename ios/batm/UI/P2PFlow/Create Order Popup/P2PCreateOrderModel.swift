import Foundation

struct P2PCreateOrderData {
    let cryptoAmount: Double
    let feeAmount: Double
}

struct P2PCreateOrderValidationError {
    let message: String
}

typealias CreateOrderModelResult = (data: P2PCreateOrderData, error: P2PCreateOrderValidationError?)

protocol P2PCreateOrderModelInput: AnyObject {
    func calculateOrderData(trade: Trade,
                            fiatAmount: Double,
                            platformFee: Double,
                            reservedBalance: Double) -> CreateOrderModelResult
                            
}

class P2PCreateOrderModel: P2PCreateOrderModelInput {
    
    func calculateOrderData(trade: Trade,
                            fiatAmount: Double,
                            platformFee: Double,
                            reservedBalance: Double) -> CreateOrderModelResult {
        let cryptoAmount = calculateCryptoAmount(fiatAmount, trade.price ?? 0)
        let feeAmount = calculateFeeAmount(cryptoAmount, platformFee)
        let error = validate(trade: trade,
                             fiatAmount: fiatAmount,
                             cryptoAmount: cryptoAmount,
                             reservedBalance: reservedBalance)
        
        let data = P2PCreateOrderData(cryptoAmount: cryptoAmount, feeAmount: feeAmount)
        
        return (data, error)
    }
    
    private func calculateCryptoAmount(_ fiatAmount: Double,_ tradePrice: Double) -> Double {
        let cryptoAmount = fiatAmount / tradePrice
        return cryptoAmount
    }
    
    private func calculateFeeAmount(_ cryptoAmount: Double,_ patformFee: Double) -> Double {
        let feeAmount = (cryptoAmount * patformFee) / 100
        return feeAmount
    }
    
    private func validate(trade: Trade, fiatAmount: Double, cryptoAmount: Double, reservedBalance: Double) -> P2PCreateOrderValidationError? {
        var error: P2PCreateOrderValidationError?
        isFiatAmountValid(trade: trade, fiatAmount: fiatAmount, error: &error)
        isReservedBalanceValid(reservedBalance: reservedBalance, cryptoAmount: cryptoAmount, error: &error)
        return error
    }
    
    @discardableResult
    private func isFiatAmountValid(trade: Trade, fiatAmount: Double, error: inout P2PCreateOrderValidationError?) -> Bool {
        guard let min = trade.minLimit, let max = trade.maxLimit else { return true }
        if min...max ~= fiatAmount { return true }
        error = P2PCreateOrderValidationError(message: localize(L.P2p.Order.Create.Fiat.error))
        return false
    }
    
    @discardableResult
    private func isReservedBalanceValid(reservedBalance: Double, cryptoAmount: Double, error: inout P2PCreateOrderValidationError? ) -> Bool {
        if cryptoAmount <= reservedBalance { return true }
        error = P2PCreateOrderValidationError(message: localize(L.P2p.Order.Create.Reserved.error))
        return false
    }
    
}

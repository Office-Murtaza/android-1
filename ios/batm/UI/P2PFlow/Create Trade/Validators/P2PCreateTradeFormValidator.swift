import Foundation

class P2PCreateTradeFormValidator {
    
    var validators = [P2PCreateTradeVlidatorBase]()
    
    func register(view: P2PCreateTradeVlidatorDelegate,
                  validator: P2PCreateTradeVlidatorBase) {
        validator.delegate = view
        validators.append(validator)
    }
    
    func isFormValid() -> Bool {
        for v in validators {
            if v.isValid == false {
                return false
            }
        }
        
        return true
    }
    
    func validate() {
        validators.forEach { $0.check() }
    }
}

protocol P2PCreateTradeVlidatorDelegate: AnyObject {
    func showErrorMessage(_ message: String)
    func hideError()
}

class P2PCreateTradeVlidatorBase {
    weak var delegate: P2PCreateTradeVlidatorDelegate?
    var isValid: Bool = false
    func check() {}
}

class P2PCreateTradeCoinsValidator:  P2PCreateTradeVlidatorBase {
    var coins: Double = 0
    var selectedCoin: CustomCoinType = .bitcoin
    var userTrades = [Trade]()
    var tradeType: P2PSellBuyViewType = .buy
    //MARK: - setup
    
    func setup(trades: [Trade], userId: Int) {
        self.userTrades = trades.filter { $0.makerUserId == userId }
    }
    
    //MARK: - updates
    func update(coins: Double) {
        self.coins = coins
    }
    
    func update(coinType: CustomCoinType) {
        self.selectedCoin = coinType
    }
    
    func update(tradeType: P2PSellBuyViewType) {
        self.tradeType = tradeType
    }
    
    override func check() {
        
        
        //MARK: - amount validation
        isValid = !coins.isZero
        
        if coins.isZero {
            delegate?.showErrorMessage("Must be not empty")
        } else {
            delegate?.hideError()
        }
        
        //MARK: - coin type
        
        let trades = userTrades.filter { $0.coin == selectedCoin.code }
        let tradesWithType = trades.filter { $0.type == tradeType.rawValue }
        
        if tradesWithType.isNotEmpty {
             isValid = false
            delegate?.showErrorMessage("You already have a \(selectedCoin.code) \(tradeType.title) trade")
        }
        
    }
}

class  P2PCreateTradePaymentValidator: P2PCreateTradeVlidatorBase {
    var tags = [P2PTagView]()
    
    func update(paymentView: P2PTagView) {
        tags.append(paymentView)
    }
    
    override func check() {
        for tag in tags {
            if tag.isSelected == true {
                isValid = true
                delegate?.hideError()
                return
            }
        }
        isValid = false
        delegate?.showErrorMessage("select one method")
    }
}

class P2PCreateTradeLimitsValidator: P2PCreateTradeVlidatorBase {
    var min: Double = 0
    var max: Double = 0
    var tradeType: P2PSellBuyViewType = .buy
    var price: Double = 0
    var reservedBalance: Double = 0
    
    func update(min: Double) {
        self.min = min
    }
    
    func update(max: Double) {
        self.max = max
    }
    
    func update(tradeType: P2PSellBuyViewType) {
        self.tradeType = tradeType
    }
    func update(price: Double) {
        self.price = price
    }
    
    func update(reservedBalance: Double) {
        self.reservedBalance = reservedBalance
    }
    
    override func check() {
        
        if min == 0, max == 0 {
            isValid = false
            delegate?.showErrorMessage("max and min should not be nil")
            return
        } else if min > max {
            isValid = false
            delegate?.showErrorMessage("max shold be more than min")
            return
        }
        
        if tradeType == .sell {
            let isReservedValid = reservedBalance >= max / price
            if isReservedValid == false {
                isValid = false
                delegate?.showErrorMessage("Max limit exceeds reserved balance")
                return
            } 
        }
        
        
        isValid = true
        delegate?.hideError()
    }
}

class  P2PCreateTradeTermsValidator: P2PCreateTradeVlidatorBase {
    var terms: String?
    
    func update(terms: String) {
        self.terms = terms
    }
    
    override func check() {
        isValid = !(terms?.isEmpty ?? true)
        
        if isValid == false {
            delegate?.showErrorMessage("select one method")
        } else {
            delegate?.hideError()
        }
    }
}





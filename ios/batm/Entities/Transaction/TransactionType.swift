import UIKit

enum TransactionType: Int {
    case unknown = 0
    case deposit = 1
    case withdraw = 2
    case sendTransfer = 3
    case receiveTransfer = 4
    case buy = 5
    case sell = 6
    case move = 7
    case sendSwap = 8
    case receiveSwap = 9
    case reserve = 10
    case recall = 11
    case `self` = 12
    case createStake = 13
    case cancelStake = 14
    case withdrawStake = 15
    
    var verboseValue: String {
        switch self {
        case .unknown: return localize(L.CoinDetails.unknown)
        case .deposit: return localize(L.CoinDetails.deposit)
        case .withdraw: return localize(L.CoinDetails.withdraw)
        case .sendTransfer: return localize(L.CoinDetails.sendGift)
        case .receiveTransfer: return localize(L.CoinDetails.receiveGift)
        case .buy: return localize(L.CoinDetails.buy)
        case .sell: return localize(L.CoinDetails.sell)
        case .move: return localize(L.CoinDetails.move)
        case .sendSwap: return localize(L.CoinDetails.sendSwap)
        case .receiveSwap: return localize(L.CoinDetails.receiveSwap)
        case .reserve: return localize(L.CoinDetails.reserve)
        case .recall: return localize(L.CoinDetails.recall)
        case .self: return localize(L.CoinDetails.se1f)
        case .createStake: return localize(L.CoinDetails.createStake)
        case .cancelStake: return localize(L.CoinDetails.cancelStake)
        case .withdrawStake: return localize(L.CoinDetails.withdrawStake)
        }
    }
    
    var associatedColor: TransactionTypeColorPalette.Type {
        switch self {
        case .unknown: return UIColor.TransactionTypeColor.Gray.self
        case .deposit: return UIColor.TransactionTypeColor.Green.self
        case .withdraw: return UIColor.TransactionTypeColor.Yellow.self
        case .sendTransfer: return UIColor.TransactionTypeColor.Yellow.self
        case .receiveTransfer: return UIColor.TransactionTypeColor.Green.self
        case .buy: return UIColor.TransactionTypeColor.Green.self
        case .sell: return UIColor.TransactionTypeColor.Yellow.self
        case .move: return UIColor.TransactionTypeColor.Blue.self
        case .sendSwap: return UIColor.TransactionTypeColor.Yellow.self
        case .receiveSwap: return UIColor.TransactionTypeColor.Green.self
        case .reserve: return UIColor.TransactionTypeColor.Yellow.self
        case .recall: return UIColor.TransactionTypeColor.Green.self
        case .self: return UIColor.TransactionTypeColor.Blue.self
        case .createStake: return UIColor.TransactionTypeColor.Yellow.self
        case .cancelStake: return UIColor.TransactionTypeColor.Blue.self
        case .withdrawStake: return UIColor.TransactionTypeColor.Green.self
        }
    }
}

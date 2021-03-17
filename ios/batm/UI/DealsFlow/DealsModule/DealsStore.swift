import Foundation

enum DealsAction: Equatable {
    case setupCoin(BTMCoin)
    case setupCoinBalances([CoinBalance])
    case setupCoinDetails(CoinDetails)
    case loadedTrades(Trades, Account)
}

struct DealsState: Equatable {
    var coinBalances: [CoinBalance]?
    var coinDetails: CoinDetails?
    var coin: BTMCoin?
    var trades: Trades?
    var userId: Int?
    var coinBalance: CoinBalance? {
        return coinBalances?.first { $0.type == coin?.type }
    }
}

struct TradesData {
    var trades: Trades?
    var userId: Int?
}

final class DealsStore: ViewStore<DealsAction, DealsState> {
    override var initialState: DealsState {
        return DealsState()
    }
    
    override func reduce(state: DealsState, action: DealsAction) -> DealsState {
        var state = state
        
        switch action {
        case let .setupCoinBalances(coinBalances): state.coinBalances = coinBalances
        case let .setupCoinDetails(coinDetails): state.coinDetails = coinDetails
        case let .setupCoin(coin): state.coin = coin
        case let .loadedTrades(trades, account):
            state.trades = trades
            state.userId = account.userId
        }
        
        return state
    }
}

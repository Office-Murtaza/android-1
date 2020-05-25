import Foundation

enum TradesAction: Equatable {
  case setupCoinBalance(CoinBalance)
  case startFetchingBuyTrades
  case startFetchingSellTrades
  case finishFetchingBuyTradesWithError
  case finishFetchingSellTradesWithError
  case finishFetchingBuyTrades(BuySellTrades)
  case finishFetchingSellTrades(BuySellTrades)
  case finishFetchingNextBuyTrades(BuySellTrades)
  case finishFetchingNextSellTrades(BuySellTrades)
  case updateBuyTradesPage(Int)
  case updateSellTradesPage(Int)
}

struct TradesState: Equatable {
  
  var coinBalance: CoinBalance?
  var buyTrades: BuySellTrades?
  var sellTrades: BuySellTrades?
  var buyTradesPage: Int = 0
  var sellTradesPage: Int = 0
  var isFetchingBuyTrades: Bool = false
  var isFetchingSellTrades: Bool = false
  
  var nextBuyTradesPage: Int {
    return buyTradesPage + 1
  }
  
  var nextSellTradesPage: Int {
    return sellTradesPage + 1
  }
  
  var isLastBuyTradesPage: Bool {
    guard let buyTrades = buyTrades else { return false }
    return buyTrades.trades.count >= buyTrades.total
  }
  
  var isLastSellTradesPage: Bool {
    guard let sellTrades = sellTrades else { return false }
    return sellTrades.trades.count >= sellTrades.total
  }
  
}

final class TradesStore: ViewStore<TradesAction, TradesState> {
  
  override var initialState: TradesState {
    return TradesState()
  }
  
  override func reduce(state: TradesState, action: TradesAction) -> TradesState {
    var state = state
    
    switch action {
    case let .setupCoinBalance(coinBalance): state.coinBalance = coinBalance
    case .startFetchingBuyTrades: state.isFetchingBuyTrades = true
    case .startFetchingSellTrades: state.isFetchingSellTrades = true
    case .finishFetchingBuyTradesWithError: state.isFetchingBuyTrades = false
    case .finishFetchingSellTradesWithError: state.isFetchingSellTrades = false
    case let.finishFetchingBuyTrades(buyTrades):
      state.isFetchingBuyTrades = false
      state.buyTrades = buyTrades
    case let.finishFetchingSellTrades(sellTrades):
      state.isFetchingSellTrades = false
      state.sellTrades = sellTrades
    case let .finishFetchingNextBuyTrades(buyTrades):
      state.isFetchingBuyTrades = false
      
      var mergedTrades = buyTrades
      
      if let currentTrades = state.buyTrades {
        mergedTrades = currentTrades
        mergedTrades.total = buyTrades.total
        mergedTrades.trades.append(contentsOf: buyTrades.trades)
      }
      
      state.buyTrades = mergedTrades
    case let .finishFetchingNextSellTrades(sellTrades):
      state.isFetchingBuyTrades = false
      
      var mergedTrades = sellTrades
      
      if let currentTrades = state.sellTrades {
        mergedTrades = currentTrades
        mergedTrades.total = sellTrades.total
        mergedTrades.trades.append(contentsOf: sellTrades.trades)
      }
      
      state.sellTrades = mergedTrades
    case let .updateBuyTradesPage(page): state.buyTradesPage = page
    case let .updateSellTradesPage(page): state.sellTradesPage = page
    }
    
    return state
  }
}

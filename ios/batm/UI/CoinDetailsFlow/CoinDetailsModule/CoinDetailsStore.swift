import Foundation

enum CoinDetailsAction: Equatable {
  case setupCoinBalance(CoinBalance)
  case startFetching
  case finishFetching
  case finishFetchingTransactions(Transactions)
  case finishFetchingNextTransactions(Transactions)
  case finishFetchingCoin(BTMCoin)
  case updatePage(Int)
}

struct CoinDetailsState: Equatable {
  
  var coinBalance: CoinBalance?
  var transactions: Transactions?
  var coin: BTMCoin?
  var page: Int = 0
  var isFetching: Bool = false
  
  var nextPage: Int {
    return page + 1
  }
  
  var isLastPage: Bool {
    guard let transactions = transactions else { return false }
    return transactions.transactions.count >= transactions.total 
  }
  
}

final class CoinDetailsStore: ViewStore<CoinDetailsAction, CoinDetailsState> {
  
  override var initialState: CoinDetailsState {
    return CoinDetailsState()
  }
  
  override func reduce(state: CoinDetailsState, action: CoinDetailsAction) -> CoinDetailsState {
    var state = state
    
    switch action {
    case let .setupCoinBalance(coinBalance): state.coinBalance = coinBalance
    case .startFetching: state.isFetching = true
    case .finishFetching: state.isFetching = false
    case let.finishFetchingTransactions(transactions):
      state.isFetching = false
      state.transactions = transactions
    case let .finishFetchingNextTransactions(transactions):
      state.isFetching = false
      
      var mergedTransactions = transactions
      
      if let currentTransactions = state.transactions {
        mergedTransactions = currentTransactions
        mergedTransactions.total = transactions.total
        mergedTransactions.transactions.append(contentsOf: transactions.transactions)
      }
      
      state.transactions = mergedTransactions
    case let .finishFetchingCoin(coin):
      state.coin = coin
    case let .updatePage(page): state.page = page
    }
    
    return state
  }
}

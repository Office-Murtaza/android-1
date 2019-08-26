import Foundation

enum CoinDetailsAction: Equatable {
  case startFetching
  case finishFetchingTransactions(Transactions)
  case finishFetchingNextTransactions(Transactions)
  case finishFetchingCoin(BTMCoin)
  case updatePage(Int)
}

struct CoinDetailsState: Equatable {
  
  var transactions: Transactions?
  var coin: BTMCoin?
  var page: Int = 0
  var isFetching: Bool = false
  
  var isLastPage: Bool {
    guard let transactions = transactions else { return false }
    return transactions.total <= transactions.transactions.count
  }
  
}

final class CoinDetailsStore: ViewStore<CoinDetailsAction, CoinDetailsState> {
  
  override var initialState: CoinDetailsState {
    return CoinDetailsState()
  }
  
  override func reduce(state: CoinDetailsState, action: CoinDetailsAction) -> CoinDetailsState {
    var state = state
    
    switch action {
    case .startFetching: state.isFetching = true
    case let.finishFetchingTransactions(transactions): state.transactions = transactions
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
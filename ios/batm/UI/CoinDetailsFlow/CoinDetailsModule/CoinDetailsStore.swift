import Foundation

enum SelectedPeriod {
  case oneDay
  case oneWeek
  case oneMonth
  case threeMonths
  case oneYear
}

enum CoinDetailsAction: Equatable {
  case setupCoinBalances([CoinBalance])
  case setupCoinDetails(CoinDetails)
  case setupPriceChartData(PriceChartData)
  case updateSelectedPeriod(SelectedPeriod)
  case startFetching
  case finishFetching
  case finishFetchingTransactions(Transactions)
  case finishFetchingNextTransactions(Transactions)
  case finishFetchingCoin(BTMCoin)
  case updatePage(Int)
}

struct CoinDetailsState: Equatable {
  
  var coinBalances: [CoinBalance]?
  var coinDetails: CoinDetails?
  var priceChartData: PriceChartData?
  var selectedPeriod: SelectedPeriod = .oneDay
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
  
  var coinBalance: CoinBalance? {
    return coinBalances?.first { $0.type == coin?.type }
  }
  
}

final class CoinDetailsStore: ViewStore<CoinDetailsAction, CoinDetailsState> {
  
  override var initialState: CoinDetailsState {
    return CoinDetailsState()
  }
  
  override func reduce(state: CoinDetailsState, action: CoinDetailsAction) -> CoinDetailsState {
    var state = state
    
    switch action {
    case let .setupCoinBalances(coinBalances): state.coinBalances = coinBalances
    case let .setupCoinDetails(CoinDetails): state.coinDetails = CoinDetails
    case let .setupPriceChartData(data): state.priceChartData = data
    case let .updateSelectedPeriod(selectedPeriod): state.selectedPeriod = selectedPeriod
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

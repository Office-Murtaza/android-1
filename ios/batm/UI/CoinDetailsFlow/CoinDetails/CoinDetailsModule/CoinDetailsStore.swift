import Foundation

enum SelectedPeriod: Int {
    case oneDay = 1
    case oneWeek
    case oneMonth
    case threeMonths
    case oneYear
}

enum CoinDetailsAction: Equatable {
    case setupCoinBalances([CoinBalance]?, CustomCoinType)
    case setupCoinDetails(CoinDetails)
    case setupPriceChartData(PriceChartDetails?)
    case updateSelectedPeriod(SelectedPeriod, PriceChartDetails)
    case startFetching
    case finishFetching
    case finishFetchingTransactions(Transactions, TransactionDetails?)
    case finishFetchingTransactionDetails(TransactionDetails?)
    case finishFetchingCoin(BTMCoin)
    case updatePage(Int)
    case setupPredefinedData(CoinDetailsPredefinedDataConfig)
}

struct CoinDetailsPredefinedDataConfig: Equatable {
    var price: Double
    var rate: Double
    var rateToDisplay: String
    var balance: CoinBalance
    var selectedPrediod: SelectedPeriod
    var chartData: [[Double]]
}

struct CoinDetailsState: Equatable {
    var coinBalance: CoinBalance?
    var coinBalances: [CoinBalance]?
    var coinDetails: CoinDetails?
    var selectedPeriod: SelectedPeriod = .oneDay
    var transactions: Transactions?
    var coin: BTMCoin?
    var page: Int = 0
    var isFetching: Bool = false
    var priceChartDetails: PriceChartDetails?
    var currentBalance: CoinBalance?
    var predefinedData: CoinDetailsPredefinedDataConfig?
    
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
        case let .setupCoinBalances(coinBalances, coinType):
            state.coinBalances = coinBalances
            state.coinBalance = coinBalances?.first { $0.type == coinType }
            state.currentBalance = coinBalances?.first { $0.type == coinType }
        case let .setupCoinDetails(coinDetails):
            state.coinDetails = coinDetails
        case let .setupPredefinedData(data):
            state.coinDetails = data.balance.details
            state.predefinedData = data
        case let .setupPriceChartData(data):
            state.priceChartDetails = data
        case let .updateSelectedPeriod(selectedPeriod, details):
            state.selectedPeriod = selectedPeriod
            state.priceChartDetails = details
            state.predefinedData?.selectedPrediod = selectedPeriod
        case .startFetching: state.isFetching = true
        case .finishFetching: state.isFetching = false
        case let .finishFetchingTransactions(transactions, transactionDetails):
            state.isFetching = false
            var tempTransactions = transactions
            
            let transactionsId = tempTransactions.transactions.compactMap { $0.txId }
            let transactionsDbId = tempTransactions.transactions.compactMap { $0.txDbId }
            
            if let transactionDetails = transactionDetails,
               !transactionsId.contains(transactionDetails.txId ?? "")
                && !transactionsDbId.contains(transactionDetails.txDbId ?? -1) {
                
                tempTransactions.total += 1
                tempTransactions.transactions.append(transactionDetails)
            }
            
            state.transactions = tempTransactions
        case .finishFetchingTransactionDetails(let transactionDetails):
            var tempTransactions = state.transactions
            
            let transactionsId = tempTransactions?.transactions.compactMap { $0.txId }
            let transactionsDbId = tempTransactions?.transactions.compactMap { $0.txDbId }
            
            if let transactionDetails = transactionDetails,
               transactionsId?.contains(transactionDetails.txId ?? "") == false
                && transactionsDbId?.contains(transactionDetails.txDbId ?? -1) == false {
                
                tempTransactions?.total += 1
                tempTransactions?.transactions.insert(transactionDetails, at: 0)
            }
            
            state.transactions = tempTransactions
        case let .finishFetchingCoin(coin):
            state.coin = coin
        case let .updatePage(page): state.page = page
        }
        
        return state
    }
}

import UIKit
import CoreLocation

protocol TradesDataSourceDelegate: AnyObject {
    func didSelected(tradeModel: TradeViewModel, type: P2PTradesType, reservedBalance: Double)
}

class TradesDataSource: NSObject,  TradeListDataSource {
    
    var tradesViewModels = [TradeViewModel]()
    var initViewModels = [TradeViewModel]()
    private var currentType: P2PTradesType?
    private var currentUserId: String?
    weak var controller: TradeListViewController?
    weak var delegate: TradesDataSourceDelegate?
    weak var tableView: UITableView?
    var currentTotal: Double?
    var currentRate: Double?
    var balance: CoinsBalance?
    
    func setup(controller: TradeListViewController) {
        self.controller = controller
        controller.delegate = self
        setup(tableView: controller.tableView)
    }
    
    func update(balance: CoinsBalance) {
        self.balance = balance
    }
    
    private func setup(tableView: UITableView?) {
        self.tableView = tableView
        tableView?.delegate = self
        tableView?.dataSource = self
        tableView?.register(P2PTradeCell.self, forCellReuseIdentifier: P2PTradeCell.reuseIdentifier)
        tableView?.tableFooterView = UIView()
        tableView?.rowHeight = 132
    }
    
    func setup(trades: Trades, type: P2PTradesType, userId: String?) {
        currentType = type
        currentUserId = userId
        currentTotal = trades.makerTotalTrades
        currentRate = trades.makerTradingRate
  
        var listTrades = trades.trades.filter{ $0.type == type.rawValue }
        
        if let id = userId {
            listTrades = listTrades.filter { $0.makerUserId != id }
        }
        
        if type == .buy {
            listTrades = listTrades.sorted(by: { (trade1, trade2) -> Bool in
                return trade1.price ?? 0 > trade2.price ?? 0
            })
        } else {
            listTrades = listTrades.sorted(by: { (trade1, trade2) -> Bool in
                return trade1.price ?? 0 < trade2.price ?? 0
            })
        }
        
        tradesViewModels = listTrades.map{ TradeViewModel(trade: $0,
                                                         totalTrades: trades.makerTotalTrades ?? 0,
                                                         rate: trades.makerTradingRate ?? 0) }
        
        initViewModels = tradesViewModels
        
        tableView?.reloadData()
    }
  
    func update(trade: Trade) {
        
       guard trade.type == currentType?.rawValue, currentUserId != trade.makerUserId else { return }
      
        let appendModel = TradeViewModel(trade: trade, totalTrades: currentTotal ?? 0, rate: currentRate ?? 0)
        
        if let index = tradesViewModels.firstIndex(where: { $0.trade.id == trade.id }) {
            tradesViewModels.remove(at: index)
        }

        tradesViewModels.insert(appendModel, at: 0)
        initViewModels = tradesViewModels
        
        DispatchQueue.main.async { [weak self] in
            self?.tableView?.reloadData()
        }
    }
    
    func reload(location: CLLocation?) {
        DispatchQueue.global(qos: .background).async { [weak self] in
            self?.tradesViewModels.forEach{ $0.update(location: location) }
            DispatchQueue.main.async { [weak self] in
                self?.tableView?.reloadData()
            }
        }
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return tradesViewModels.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(withIdentifier: P2PTradeCell.reuseIdentifier, for: indexPath) as? P2PTradeCell else {
            return UITableViewCell()
        }
        let vm = tradesViewModels[indexPath.row]
        cell.update(viewModel: vm)
        cell.selectionStyle = .none
        return cell
    }
  
  func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    let tradeModel = tradesViewModels[indexPath.row]
    if let type = currentType {
        let coinBalance = balance?.coins.first(where: { (balance) -> Bool in
            balance.type == tradeModel.coin
        })
        let reservedBalance: Double = (coinBalance?.reservedBalance ?? 0).doubleValue
        delegate?.didSelected(tradeModel: tradeModel, type: type, reservedBalance: reservedBalance)
        
    }
  }
  
}

extension TradesDataSource: TradeListDelegate {
    func applyFilter(scope: FilterScopeModel) {
        if scope.isEmpty {
            tradesViewModels = initViewModels
        } else {
            tradesViewModels = initViewModels.filter { $0.isInclude(scope) }
            if scope.sortType.isNotEmpty {
                for type in scope.sortType {
                    if type == .price {
                        tradesViewModels.sort { $0.price > $1.price }
                    }
                    if type == .distance {
                        tradesViewModels.sort { $0.distance ?? 0 < $1.distance ?? 0 }
                    }
                }
            }
        }
        
        tableView?.reloadData()
    }
    
    func resetFilter() {
        tradesViewModels = initViewModels
        tableView?.reloadData()
    }
}

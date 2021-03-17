import UIKit
import CoreLocation


class TradesDataSource: NSObject,  TradeListDataSource {
    
    var tradesViewModels = [TradeViewModel]()
    var initViewModels = [TradeViewModel]()
    
    weak var controller: TradeListViewController?
    
    weak var tableView: UITableView?
    
    func setup(controller: TradeListViewController) {
        self.controller = controller
        controller.delegate = self
        setup(tableView: controller.tableView)
    }
    
    private func setup(tableView: UITableView?) {
        self.tableView = tableView
        tableView?.delegate = self
        tableView?.dataSource = self
        tableView?.register(P2PTradeCell.self, forCellReuseIdentifier: P2PTradeCell.reuseIdentifier)
        tableView?.tableFooterView = UIView()
        tableView?.rowHeight = 132
    }
    
    func setup(trades: Trades, type: P2PTradesType, userId: Int?) {
        
        var buyTrades = trades.trades.filter{ $0.type == type.rawValue }
        
        if let id = userId {
            buyTrades = buyTrades.filter { $0.makerId != id }
        }
        
        if type == .buy {
            buyTrades = buyTrades.sorted(by: { (trade1, trade2) -> Bool in
                return trade1.price ?? 0 > trade2.price ?? 0
            })
        } else {
            buyTrades = buyTrades.sorted(by: { (trade1, trade2) -> Bool in
                return trade1.price ?? 0 < trade2.price ?? 0
            })
        }
        
        tradesViewModels = buyTrades.map{ TradeViewModel(trade: $0,
                                                         totalTrades: trades.makerTotalTrades,
                                                         rate: trades.makerTradingRate) }
        
        initViewModels = tradesViewModels
        
        tableView?.reloadData()
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

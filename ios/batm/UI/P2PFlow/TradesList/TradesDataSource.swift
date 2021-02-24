import UIKit
import CoreLocation


class TradesDataSource: NSObject,  TradeListDataSource {
    
    var tradesViewModels = [TradeViewModel]()

    weak var tableView: UITableView?

    func setup(tableView: UITableView?) {
        self.tableView = tableView
        tableView?.delegate = self
        tableView?.dataSource = self
        tableView?.register(P2PTradeCell.self, forCellReuseIdentifier: P2PTradeCell.reuseIdentifier)
        tableView?.tableFooterView = UIView()
        tableView?.rowHeight = 132
    }
    
    func setup(trades: Trades, type: P2PTradesType) {
        let buyTrades = trades.trades.filter{ $0.type == type.rawValue }
        tradesViewModels = buyTrades.map{ TradeViewModel(trade: $0,
                                                         totalTrades: trades.totalTrades,
                                                         rate: trades.tradingRate) }
        tableView?.reloadData()
    }
    
    func reload(location: CLLocation?) {
        tradesViewModels.forEach{ $0.currentLocation = location }
        tableView?.reloadData()
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
        
        return cell
    }
}

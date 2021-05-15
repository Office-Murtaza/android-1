import UIKit
import CoreLocation

class MyOpenOrdersViewController: UIViewController {
    
    private let dataSource = MyOpenOrdersDataSource()
    private let emptyView = OpenOrdersEmptyView()
    private var trades: Trades?
    
    
    private lazy var tableView: UITableView = {
        let table = UITableView()
        table.separatorStyle = .none
        view.addSubview(table)
        return table
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupLayout()
    }
    
    func update(orders: [Order], trades: Trades) {
        let viewModels = orders.sorted { $0.timestamp ?? 0 > $1.timestamp ?? 0 }.map { MyOpenOrdersCellViewModel(order: $0) }
        self.trades = trades
        dataSource.udpate(vm: viewModels)
    }
    
    func update(location: CLLocation?) {
        dataSource.reload(location: location)
    }
    
    private func setupUI() {
        emptyView.isHidden = dataSource.viewModels.isNotEmpty
        
        dataSource.setup(tableView: tableView)
        dataSource.delegate = self
        
        view.addSubviews([
            tableView,
            emptyView
        ])
    }
    
    private func setupLayout() {
        tableView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
        
        emptyView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    }
    
    func presentOrderDetails(vm: MyOpenOrdersCellViewModel) {
        let orderDetails = P2POrderDetailsViewController()
        orderDetails.setup(order: vm.order, distance: vm.distanceInMiles ?? "", myRate: trades?.makerTradingRate.toString() ?? "0")
        navigationController?.pushViewController(orderDetails, animated: true)
    }
    
}

extension MyOpenOrdersViewController: MyOpenOrdersDataSourceDelegate {
    func didSelected(vm: MyOpenOrdersCellViewModel) {
       presentOrderDetails(vm: vm)
    }
}

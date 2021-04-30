import UIKit

class MyOpenOrdersViewController: UIViewController {
    
    private let dataSource = MyOpenOrdersDataSource()
    private let emptyView = OpenOrdersEmptyView()
    
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
    
    func update(orders: [Order]) {
        let viewModels = orders.sorted { $0.timestamp ?? 0 > $1.timestamp ?? 0 }.map { MyOpenOrdersCellViewModel(order: $0) }
        dataSource.udpate(vm: viewModels)
    }
    
    private func setupUI() {
        emptyView.isHidden = dataSource.viewModels.isNotEmpty
        dataSource.setup(tableView: tableView)
        
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
    
}

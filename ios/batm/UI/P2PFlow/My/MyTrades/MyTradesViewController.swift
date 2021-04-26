import UIKit
import SnapKit

class MyTradesViewController: UIViewController {
  
  private lazy var emptyView = MyTradesEmptyView()
  
  private lazy var tableView: UITableView = {
    let table = UITableView()
    table.separatorStyle = .none
    view.addSubview(table)
    return table
  }()
  
  private let dataSource = MyTradesDataSource()
  
  override func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
    setupLayout()
  }
  
  
  func update(trades: [Trade]) {
    let viewModels = trades.sorted { $0.timestamp ?? 0 > $1.timestamp ?? 0 }.map { MyTradesCellViewModel(trade: $0) }
    dataSource.udpate(vm: viewModels)
  }
  
  func setupUI() {
    dataSource.setup(tableView: tableView)
    dataSource.delegate = self
    emptyView.delegate = self
    emptyView.isHidden = dataSource.viewModels.isNotEmpty
    
    view.addSubviews([tableView,
                      emptyView])
  }
  
  func setupLayout() {
    tableView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    emptyView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
}

extension MyTradesViewController: MyTradesEmptyViewDelegate {
  func didTapCreateTrade() {
    print("did tap create trade")
  }
}

extension MyTradesViewController: MyTradesDataSourceDelegate {
  func didSelected(model: MyTradesCellViewModel) {
    let controller = P2PTradeDetailsViewController()
    controller.setup(trade: model.trade)
    navigationController?.pushViewController(controller, animated: true)
  }
}

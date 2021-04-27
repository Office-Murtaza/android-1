import UIKit
import SnapKit

protocol MyTradesViewControllerDelegate: class {
  func didTapCreateTrade()
}

class MyTradesViewController: UIViewController {
  
  private lazy var emptyView = MyTradesEmptyView()
  
  private lazy var tableView: UITableView = {
    let table = UITableView()
    table.separatorStyle = .none
    view.addSubview(table)
    return table
  }()
  private var balance: CoinsBalance?
  private let dataSource = MyTradesDataSource()
  
  weak var delegate: MyTradesViewControllerDelegate?
  
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
  
  func update(balance: CoinsBalance) {
    self.balance = balance
  }
}

extension MyTradesViewController: MyTradesEmptyViewDelegate {
  func didTapCreateTrade() {
    delegate?.didTapCreateTrade()
  }
}

extension MyTradesViewController: MyTradesDataSourceDelegate {
  func didSelected(model: MyTradesCellViewModel) {
    guard let balance = self.balance else { return }
    let controller = P2PTradeDetailsEditViewController()
    controller.setup(trade: model.trade, balance: balance)
    navigationController?.pushViewController(controller, animated: true)
  }
}

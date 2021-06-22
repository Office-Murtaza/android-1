import UIKit
import CoreLocation

protocol MyOpenOrdersViewControllerDelegate: AnyObject {
  func didTapDistance(order: Order)
  func didTap(type: OrderDetailsActionType, model: MyOrderViewModel)
  func selectedRate(orderModel: MyOrderViewModel, rate: Int)
}

class MyOpenOrdersViewController: UIViewController {
  
  weak var delegate: MyOpenOrdersViewControllerDelegate?
  
  private let dataSource = MyOpenOrdersDataSource()
  private let emptyView = OpenOrdersEmptyView()
  private var trades: Trades?
  private var currentLocation: CLLocation?
  private weak var orderDetails: P2POrderDetailsViewController?
  private var userId = ""
  
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
  
  func update(orders: [Order], trades: Trades, userId: String) {
    self.userId = userId
    let viewModels = orders.sorted { $0.timestamp ?? 0 > $1.timestamp ?? 0 }.map { MyOrderViewModel(order: $0, userId: userId) }
    viewModels.forEach { (vm) in
      if let associatedTradeType = trades.trades.first(where: {$0.id == vm.order.tradeId})?.type,
         let type = P2PSellBuyViewType(rawValue: associatedTradeType) {
        vm.upate(type: type)
      }
    }
    self.trades = trades
    dataSource.udpate(vm: viewModels)
    emptyView.isHidden = dataSource.viewModels.isNotEmpty
  }
  
  func update(location: CLLocation?) {
    currentLocation = location
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
 
  func presentOrderDetails(vm: MyOrderViewModel) {
   let orderDetails = P2POrderDetailsViewController()
    orderDetails.delegate = self
    vm.update(location: currentLocation)
    orderDetails.setup(viewModel: vm,
                        myRate: trades?.makerTradingRate.toString() ?? "0")
    navigationController?.pushViewController(orderDetails, animated: true)
    self.orderDetails = orderDetails
  }
  
  func updateWithUpdatedOrder(_ order: Order) {
    dataSource.updateModels(order: order, userId: userId)
    if let presentedOrder = orderDetails?.order, presentedOrder.id == order.id {
      orderDetails?.update(order: order)
    }
    emptyView.isHidden = dataSource.viewModels.isNotEmpty
  }
  
}

extension MyOpenOrdersViewController: MyOpenOrdersDataSourceDelegate {
  func didSelected(vm: MyOrderViewModel) {
    vm.update(location: currentLocation)
    presentOrderDetails(vm: vm)
  }
}

extension MyOpenOrdersViewController: P2POrderDetailsViewControllerDelegate {
  func didTapDistance(order: Order) {
    delegate?.didTapDistance(order: order)
  }
  
  func didTap(type: OrderDetailsActionType, orderModel: MyOrderViewModel) {
    delegate?.didTap(type: type, model: orderModel)
  }
  
  func selectedRate(orderModel: MyOrderViewModel, rate: Int) {
    delegate?.selectedRate(orderModel: orderModel, rate: rate)
  }
  
}

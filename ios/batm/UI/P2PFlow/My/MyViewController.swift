import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents
import CoreLocation

protocol MyViewControllerDelegate: AnyObject {
  func didTapCreateTrade()
  func didSelectEdit(data: P2PEditTradeDataModel)
  func cancelTrade(id: String)
  func didTapDistance(order: Order)
  func didTap(type: OrderDetailsActionType, model: MyOrderViewModel)
  func selectedRate(orderModel: MyOrderViewModel, rate: Int)
}

class MyViewController: UIViewController, MDCTabBarDelegate {
  
  private let myTradesViewController = MyTradesViewController()
  private let openOrdersViewController = MyOpenOrdersViewController()
  private let infoViewController = MyInfoViewController()
  private var controllers = [UIViewController]()
  private var prevIndex = 0
  private var balance: CoinsBalance?
  private var currentLocation: CLLocation?
  private var userId: Int?
  
  weak var delegate: MyViewControllerDelegate?
  
  lazy var pageController: UIPageViewController = {
    let controller = UIPageViewController(transitionStyle: .scroll,
                                          navigationOrientation: .horizontal,
                                          options: .none)
    controller.view.backgroundColor = .white
    addChild(controller)
    view.addSubview(controller.view)
    controller.didMove(toParent: self)
    return controller
  }()
  
  let tradesItem = UITabBarItem(title: localize(L.P2p.Tabbar.Trades.title), image: nil, tag: 0)
  let openOrdersItem = UITabBarItem(title: localize(L.P2p.Tabbar.OpenOrders.title), image: nil, tag: 1)
  let infoItem = UITabBarItem(title: localize(L.P2p.Tabbar.Info.title), image: nil, tag: 2)
  
  lazy var tabBar: MDCTabBar = {
    let tabBar = MDCTabBar()
    tabBar.itemAppearance = .titles
    tabBar.alignment = .justified
    tabBar.setTitleColor(.black, for: .selected)
    tabBar.setTitleColor(UIColor(hexString: "000000").withAlphaComponent(0.64), for: .normal)
    tabBar.backgroundColor = .lightGray
    tabBar.tintColor = .blue
    tabBar.items = [tradesItem, openOrdersItem, infoItem]
    return tabBar
  }()
  
  override func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
    setupLayout()
  }
  
  func update(trades: Trades, userId: Int?) {
    self.userId = userId
    let myTrades = trades.trades.filter { $0.makerUserId == userId }
    let myOrders = trades.orders.filter { $0.makerUserId == userId || $0.takerUserId == userId }
    myTradesViewController.update(trades: myTrades)
    if let id = userId {
      openOrdersViewController.update(orders: myOrders, trades: trades, userId: id)
    }
    let verificationStatus = TradeVerificationStatus(rawValue: trades.makerStatus )
    infoViewController.update(id: trades.makerPublicId,
                              verificationImage: verificationStatus?.image,
                              verificationStatus: verificationStatus?.status ?? "",
                              rate: (trades.makerTradingRate ?? 0).coinFormatted,
                              total: (trades.makerTotalTrades ?? 0).coinFormatted)
  }
  
  func update(balance: CoinsBalance) {
    self.balance = balance
    self.myTradesViewController.update(balance: balance)
  }
  
  func update(location: CLLocation?) {
    currentLocation = location
    openOrdersViewController.update(location: location)
  }
  
  func openOrder(order: Order) {
    guard let id = userId else { return }
    tabBar.setSelectedItem(self.openOrdersItem, animated: true)
    tabBar(tabBar,didSelect: self.openOrdersItem)
    let orderModel = MyOrderViewModel(order: order, userId: id)
    orderModel.update(location: currentLocation)
    self.openOrdersViewController.presentOrderDetails(vm: orderModel)
  }
  
  func updateWithUpdatedOrder(_ order: Order) {
    openOrdersViewController.updateWithUpdatedOrder(order)
  }
  
  private func setupUI() {
    tabBar.delegate = self
    
    myTradesViewController.delegate = self
    openOrdersViewController.delegate = self
    
    view.addSubview(tabBar)
    
    controllers = [
      myTradesViewController,
      openOrdersViewController,
      infoViewController
    ]
    
    pageController.setViewControllers([controllers[0]], direction: .forward, animated: true) { (result) in
      print(result)
    }
  }
  
  private func setupLayout() {
    tabBar.snp.makeConstraints {
      $0.top.equalToSuperview()
      $0.left.right.equalToSuperview()
    }
    
    pageController.view.snp.makeConstraints {
      $0.top.equalTo(tabBar.snp.bottom).offset(0)
      $0.left.right.equalToSuperview()
      $0.bottom.equalToSuperview()
    }
    
  }
  //MARK: - Tabbar delegate
  
  
  func tabBar(_ tabBar: MDCTabBar, didSelect item: UITabBarItem) {
    let direction: UIPageViewController.NavigationDirection = prevIndex < item.tag ? .forward : .reverse
    pageController.setViewControllers([controllers[item.tag]], direction: direction, animated: true) { [weak self, item] (result) in
      print(result)
      self?.prevIndex = item.tag
    }
  }
}

extension MyViewController: MyTradesViewControllerDelegate {
  
    func didTapCreateTrade() {
        delegate?.didTapCreateTrade()
    }
    
    func didSelectEdit(data: P2PEditTradeDataModel) {
        delegate?.didSelectEdit(data: data)
    }
    
    func cancelTrade(id: String) {
        delegate?.cancelTrade(id: id)
    }
    
}

extension MyViewController: MyOpenOrdersViewControllerDelegate {
  
  func didTapDistance(order: Order) {
    delegate?.didTapDistance(order: order)
  }
  
  func didTap(type: OrderDetailsActionType, model: MyOrderViewModel) {
    delegate?.didTap(type: type, model: model)
  }
  
  func selectedRate(orderModel: MyOrderViewModel, rate: Int) {
    delegate?.selectedRate(orderModel: orderModel, rate: rate)
  }
}

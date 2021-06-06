import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents
import MessageUI
import CoreLocation

class P2PViewController: ModuleViewController<P2PPresenter>, MDCTabBarDelegate {
    
    public var buyViewController: TradeListViewController?
    private let buyDataSource = TradesDataSource()
    
    public var sellViewController: TradeListViewController?
    private let sellDataSource = TradesDataSource()
    
    public var myViewController: MyViewController?
    
    private var controllers = [UIViewController]()
    private var prevIndex = 0
    private var balance: CoinsBalance?
    var currentLocation: CLLocation?
    
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
    
  let buyTradesItem = UITabBarItem(title: localize(L.P2p.Tabbar.Buy.title), image: nil, tag: 0)
  let sellTradesItem = UITabBarItem(title: localize(L.P2p.Tabbar.Sell.title), image: nil, tag: 1)
  let myTradesItem = UITabBarItem(title: localize(L.P2p.Tabbar.My.title), image: nil, tag: 2)
    
    lazy var tabBar: MDCTabBar = {
        let tabBar = MDCTabBar()
        tabBar.itemAppearance = .titles
        tabBar.alignment = .justified
        tabBar.setTitleColor(.white, for: .selected)
        tabBar.setTitleColor(.white, for: .normal)
        tabBar.backgroundColor = UIColor(hexString: "#0073E4")
        tabBar.tintColor = .white
        tabBar.items = [buyTradesItem, sellTradesItem, myTradesItem]
        return tabBar
    }()
    
  
  override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    presenter.willHideModule()
  }
  
  override func setupUI() {
    
    myViewController?.delegate = self
    title = localize(L.P2p.Trading.Vc.title)
    
    navigationItem.rightBarButtonItem = UIBarButtonItem(image: UIImage(named: "p2p_create_trade"),
                                                        style: .plain,
                                                        target: self,
                                                        action: #selector(createTrade))
    
    presenter.checkLocation()
    tabBar.delegate = self
    view.addSubview(tabBar)
    controllers = [
      buyViewController!,
      sellViewController!,
      myViewController!
    ]
    pageController.setViewControllers([controllers[0]], direction: .forward, animated: true) { (result) in
      print(result)
    }
    
    
    guard let buyController = buyViewController, let sellController = sellViewController else { return }
    
    buyDataSource.setup(controller: sellController)
    sellDataSource.setup(controller: buyController)
    buyDataSource.delegate = self
    sellDataSource.delegate = self
 
  }
    
    @objc func createTrade() {
        guard let balance = balance,
              let trades = presenter.trades.value,
              let id = presenter.userId else { return }
        
        let controller = P2PCreateTradeViewController(trades: trades,
                                                      userId: id,
                                                      balance: balance,
                                                      payments:TradePaymentMethods.allCases,
                                                      delegate: self)
        
        navigationController?.pushViewController(controller, animated: true)
    }
  
    override func setupBindings() {
        presenter.currentLocation
            .asObservable()
            .filterNil()
            .distinctUntilChanged()
            .observeOn(MainScheduler())
            .do { [weak self] (location) in
                DispatchQueue.main.async {
                    self?.currentLocation = location
                    self?.buyViewController?.update(location: location)
                    self?.sellViewController?.update(location: location)
                    self?.buyDataSource.reload(location: location)
                    self?.sellDataSource.reload(location: location)
                    self?.myViewController?.update(location: location)
                }
        }
            .subscribe()
            .disposed(by: disposeBag)
        presenter.trades
            .asObservable()
            .filterNil()
            .distinctUntilChanged()
            .observeOn(MainScheduler())
            .do { [unowned self] (trades) in
                let id = self.presenter.userId
                self.buyDataSource.setup(trades: trades, type: .buy, userId: id)
                self.sellDataSource.setup(trades: trades, type: .sell, userId: id)
                self.myViewController?.update(trades: trades, userId: id)
            }.subscribe()
            .disposed(by: disposeBag)

        presenter.balance.subscribeOn(MainScheduler()).subscribe { [weak self] (balance) in
            self?.balance = balance
        }.disposed(by: disposeBag)
        
        presenter.balance.subscribeOn(MainScheduler()).filterNil().subscribe { [weak self] (balance) in
            self?.myViewController?.update(balance: balance)
        }.disposed(by: disposeBag)
        
        presenter.balance.subscribeOn(MainScheduler()).filterNil().subscribe { [weak self] (balance) in
            self?.buyDataSource.update(balance: balance)
        }.disposed(by: disposeBag)
        
        presenter.balance.subscribeOn(MainScheduler()).filterNil().subscribe { [weak self] (balance) in
            self?.sellDataSource.update(balance: balance)
        }.disposed(by: disposeBag)
      
      presenter.tradeSuccessMessage
        .asObservable()
        .observeOn(MainScheduler())
        .filter{ $0.isNotEmpty }
        .subscribe { [unowned self] (event) in
          self.navigationController?.popToViewController(self, animated: true)
          let message = event.element?.description
          self.view.makeToast(message)
          self.tabBar.setSelectedItem(self.myTradesItem, animated: true)
          self.tabBar(tabBar,didSelect: self.myTradesItem)
        }.disposed(by: disposeBag)
        
        presenter.socketTrade
            .subscribe { [unowned self] (trade) in
                self.buyDataSource.update(trade: trade)
        }.disposed(by: disposeBag)
        
        presenter.socketTrade
            .subscribe { [unowned self] (trade) in
                self.sellDataSource.update(trade: trade)
        }.disposed(by: disposeBag)
      
      
      presenter.updatedOrder
          .subscribe { [unowned self] (order) in
            self.myViewController?.updateWithUpdatedOrder(order)
      }.disposed(by: disposeBag)
        
        
        presenter.createdOrder.subscribeOn(MainScheduler()).filterNil().subscribe {[weak self] (order) in
            //Sometimes not proper scheduling to main to give more safety
            DispatchQueue.main.async {
                self?.navigateToOrderDetails(order)
            }
        }.disposed(by: disposeBag)
    }
    
    override func setupLayout() {
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
    
    func navigateToOrderDetails(_ order: Order) {
        navigationController?.popToViewController(self, animated: true)
        view.makeToast(localize(L.P2p.Order.Created.message))
        
        tabBar.setSelectedItem(self.myTradesItem, animated: true)
        tabBar(tabBar,didSelect: self.myTradesItem)
        
        myViewController?.openOrder(order: order)
    }
    
    //MARK: - Tabbar delegate
    
    func tabBar(_ tabBar: MDCTabBar, didSelect item: UITabBarItem) {
        let direction: UIPageViewController.NavigationDirection = prevIndex < item.tag ? .forward : .reverse
        pageController.setViewControllers([controllers[item.tag]], direction: direction, animated: true) { [weak self, item] (result) in
            print(result)
            self?.prevIndex = item.tag
        }
    }
    
    func presentMapWithRoute(latitude: Double, longitude: Double) {
        guard let userLocation = currentLocation else { return }
        
        let url = "maps://?saddr=\(userLocation.coordinate.latitude),\(userLocation.coordinate.longitude)&daddr=\(latitude),\(longitude)"
        
        guard let openUrl = URL(string:url) else { return }
        
        UIApplication.shared.open(openUrl, options: [:], completionHandler: nil)
    }
}

extension P2PViewController: P2PCreateTradeViewControllerDelegate {
    func didSelectedSubmit(data: P2PCreateTradeDataModel) {
      presenter.didSelectedSubmit(data: data)
    }
}

extension P2PViewController: MyViewControllerDelegate {
  func didTapCreateTrade() {
    createTrade()
  }
  
  func didSelectEdit(data: P2PEditTradeDataModel) {
    presenter.editTrade(data: data)
  }
    
  func cancelTrade(id: String) {
    presenter.cancelTrade(id: id)
  }
    
  func didTapDistance(order: Order) {
    guard let latitude = order.makerLatitude, let longitude = order.makerLongitude else { return }
    presentMapWithRoute(latitude: latitude, longitude: longitude)
  }
  
  func didTap(type: OrderDetailsActionType, model: MyOrderViewModel) {
    presenter.didTap(type: type, model: model)
  }
  
}

extension P2PViewController: TradesDataSourceDelegate {
    
    func didSelected(tradeModel: TradeViewModel, type: P2PTradesType, reservedBalance: Double) {
        switch type {
        case .buy:
            let sellController = P2PTradeDetailsSellViewController()
            sellController.delegate = self
            sellController.setup(trade: tradeModel.trade, distance: "\(tradeModel.distanceInMiles ?? "0") Miles", reservedBalance: reservedBalance)
            navigationController?.pushViewController(sellController, animated: true)
            
        case .sell:
            let buyController = P2PTradeDetailsBuyViewController()
            buyController.delegate = self
            buyController.setup(trade: tradeModel.trade, distance: "\(tradeModel.distanceInMiles ?? "0") Miles", reservedBalance: reservedBalance)
            navigationController?.pushViewController(buyController, animated: true)
        }
    }
}

extension P2PViewController: P2PTradeDetailsCreateOrderDelegate {
    
    func didTapDistance(trade: Trade) {
        guard let latitude = trade.makerLatitude, let longitude = trade.makerLongitude else { return }
        presentMapWithRoute(latitude: latitude, longitude: longitude)
    }
    
    func createOrder(model: P2PCreateOrderDataModel) {
        presenter.createOrder(model: model)
    }
}


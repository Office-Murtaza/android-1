import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents
import MessageUI

class P2PViewController: ModuleViewController<P2PPresenter>, MDCTabBarDelegate {
    
    public var buyViewController: TradeListViewController?
    private let buyDataSource = TradesDataSource()
    
    public var sellViewController: TradeListViewController?
    private let sellDataSource = TradesDataSource()
    
    public var myViewController: MyViewController?
    
    private var controllers = [UIViewController]()
    private var prevIndex = 0
    private var balance: CoinsBalance?
    
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
    
    let buyTradesItem = UITabBarItem(title: "BUY", image: nil, tag: 0)
    let sellTradesItem = UITabBarItem(title: "SELL", image: nil, tag: 1)
    let openTradesItem = UITabBarItem(title: "MY...", image: nil, tag: 2)
    
    lazy var tabBar: MDCTabBar = {
        let tabBar = MDCTabBar()
        tabBar.itemAppearance = .titles
        tabBar.alignment = .justified
        tabBar.setTitleColor(.white, for: .selected)
        tabBar.setTitleColor(.white, for: .normal)
        tabBar.backgroundColor = UIColor(hexString: "#0073E4")
        tabBar.tintColor = .white
        tabBar.items = [buyTradesItem, sellTradesItem, openTradesItem]
        return tabBar
    }()
    
    override func setupUI() {
        title = "P2P Trading"

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
        
        buyDataSource.setup(controller: buyController)
        sellDataSource.setup(controller: sellController)
        
    }
    
    @objc func createTrade() {
        guard let balance = balance else { return }
        let controller = P2PCreateTradeViewController(balance: balance, payments: TradePaymentMethods.allCases, delegate: self)
        navigationController?.pushViewController(controller, animated: true)
    }
    
    override func setupBindings() {
        presenter.currentLocation
            .asObservable()
            .filterNil()
            .distinctUntilChanged()
            .observeOn(MainScheduler())
            .do { [weak self] (location) in
            self?.buyDataSource.reload(location: location)
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
        
        presenter.balance.observeOn(MainScheduler()).subscribe { [unowned self] (balance) in
            self.balance = balance
        }.disposed(by: disposeBag)
      
      presenter.dismissTopController.asObservable().observeOn(MainScheduler()).filter{ $0 != false }.subscribe { [weak self] (event) in
        self?.navigationController?.popViewController(animated: true)
      }
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
    
    //MARK: - Tabbar delegate
    
    func tabBar(_ tabBar: MDCTabBar, didSelect item: UITabBarItem) {
        let direction: UIPageViewController.NavigationDirection = prevIndex < item.tag ? .forward : .reverse
        pageController.setViewControllers([controllers[item.tag]], direction: direction, animated: true) { [weak self, item] (result) in
            print(result)
            self?.prevIndex = item.tag
        }
    }
}

extension P2PViewController: P2PCreateTradeViewControllerDelegate {
    func didSelectedSubmit(data: P2PCreateTradeDataModel) {
      presenter.didSelectedSubmit(data: data)
    }
}



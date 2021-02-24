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
    
    public var myViewController: UIViewController?
    
    var controllers = [UIViewController]()
    
    
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
        tabBar.backgroundColor = .blue
        tabBar.tintColor = .white
        tabBar.items = [buyTradesItem, sellTradesItem, openTradesItem]
        return tabBar
    }()
    
    override func setupUI() {
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
        
        buyDataSource.setup(tableView: buyViewController?.tableView)
        sellDataSource.setup(tableView: sellViewController?.tableView)
        
        
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
            .do { [weak self] (trades) in
                self?.buyDataSource.setup(trades: trades, type: .buy)
                self?.sellDataSource.setup(trades: trades, type: .sell)
            }.subscribe()
            .disposed(by: disposeBag)

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
    var prevIndex = 0
    func tabBar(_ tabBar: MDCTabBar, didSelect item: UITabBarItem) {
        let direction: UIPageViewController.NavigationDirection = prevIndex < item.tag ? .forward : .reverse
        pageController.setViewControllers([controllers[item.tag]], direction: direction, animated: true) { [weak self, item] (result) in
            print(result)
            self?.prevIndex = item.tag
        }
    }
}



import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class TradesViewController: NavigationScreenViewController<TradesPresenter>, MDCTabBarDelegate {
  
  var buyTradesDataSource: BuySellTradesTableViewDataSource!
  var sellTradesDataSource: BuySellTradesTableViewDataSource!
  
  let headerView = CoinWithdrawHeaderView()
  
  let buyTradesItem = UITabBarItem(title: "BUY TRADES", image: nil, tag: 0)
  let sellTradesItem = UITabBarItem(title: "SELL TRADES", image: nil, tag: 1)
  let openTradesItem = UITabBarItem(title: "OPEN TRADES", image: nil, tag: 2)
  
  lazy var tabBar: MDCTabBar = {
    let tabBar = MDCTabBar()
    tabBar.itemAppearance = .titles
    tabBar.alignment = .justified
    tabBar.setTitleColor(.ceruleanBlue, for: .selected)
    tabBar.setTitleColor(.slateGrey, for: .normal)
    tabBar.tintColor = .ceruleanBlue
    tabBar.items = [buyTradesItem, sellTradesItem, openTradesItem]
    return tabBar
  }()
  
  let buyTradesRefreshControl = UIRefreshControl()
  let sellTradesRefreshControl = UIRefreshControl()
  
  let buyTradesTableView = TradesTableView()
  let sellTradesTableView = TradesTableView()
  let openTradesTableView = TradesTableView()
  
  var tableViews: [TradesTableView] { return [buyTradesTableView, sellTradesTableView, openTradesTableView] }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    tabBar.delegate = self
    sellTradesTableView.isHidden = true
    openTradesTableView.isHidden = true
    
    customView.contentView.addSubviews(headerView,
                                       tabBar,
                                       buyTradesTableView,
                                       sellTradesTableView,
                                       openTradesTableView)
    customView.tapRecognizer.isEnabled = false
    
    buyTradesTableView.refreshControl = buyTradesRefreshControl
    sellTradesTableView.refreshControl = sellTradesRefreshControl
  }

  override func setupLayout() {
    customView.contentView.snp.makeConstraints {
      $0.height.equalToSuperview()
    }
    headerView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(25)
      $0.left.equalToSuperview().offset(15)
      $0.right.lessThanOrEqualToSuperview().offset(-15)
    }
    tabBar.snp.makeConstraints {
      $0.top.equalTo(headerView.snp.bottom).offset(20)
      $0.left.right.equalToSuperview()
    }
    tableViews.forEach {
      $0.snp.makeConstraints {
        $0.top.equalTo(tabBar.snp.bottom)
        $0.left.right.bottom.equalToSuperview()
      }
    }
  }
  
  func setupUIBindings() {
    buyTradesTableView.dataSource = buyTradesDataSource
    buyTradesDataSource.tableView = buyTradesTableView
    
    sellTradesTableView.dataSource = sellTradesDataSource
    sellTradesDataSource.tableView = sellTradesTableView
    
    presenter.state
      .map { $0.coinBalance?.type.code }
      .filterNil()
      .map { String(format: localize(L.Trades.title), $0) }
      .drive(onNext: { [customView] in customView.setTitle($0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coinBalance }
      .filterNil()
      .drive(onNext: { [headerView] in headerView.configure(for: $0, useReserved: true) })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.buyTrades?.trades }
      .filterNil()
      .asObservable()
      .bind(to: buyTradesDataSource.tradesRelay)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.sellTrades?.trades }
      .filterNil()
      .asObservable()
      .bind(to: sellTradesDataSource.tradesRelay)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.isFetchingBuyTrades }
      .asObservable()
      .bind(to: buyTradesRefreshControl.rx.isRefreshing)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.isFetchingSellTrades }
      .asObservable()
      .bind(to: sellTradesRefreshControl.rx.isRefreshing)
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let refreshBuyTradesDriver = buyTradesRefreshControl.rx.controlEvent(.valueChanged).asDriver()
    let refreshSellTradesDriver = sellTradesRefreshControl.rx.controlEvent(.valueChanged).asDriver()
    let showMoreBuyTradesDriver = buyTradesTableView.rx.willDisplayLastCell.asDriver(onErrorDriveWith: .empty())
    let showMoreSellTradesDriver = sellTradesTableView.rx.willDisplayLastCell.asDriver(onErrorDriveWith: .empty())
    
    presenter.bind(input: TradesPresenter.Input(back: backDriver,
                                                refreshBuyTrades: refreshBuyTradesDriver,
                                                refreshSellTrades: refreshSellTradesDriver,
                                                showMoreBuyTrades: showMoreBuyTradesDriver,
                                                showMoreSellTrades: showMoreSellTradesDriver))
  }
  
  func tabBar(_ tabBar: MDCTabBar, didSelect item: UITabBarItem) {
    buyTradesTableView.isHidden = item.tag != buyTradesItem.tag
    sellTradesTableView.isHidden = item.tag != sellTradesItem.tag
    openTradesTableView.isHidden = item.tag != openTradesItem.tag
  }
}

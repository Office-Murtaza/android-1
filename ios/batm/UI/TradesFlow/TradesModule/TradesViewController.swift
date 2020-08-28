import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class TradesViewController: NavigationScreenViewController<TradesPresenter>, MDCTabBarDelegate {
  
  let didTapRecallRelay = PublishRelay<Void>()
  let didTapReserveRelay = PublishRelay<Void>()
  let didTapCreateRelay = PublishRelay<Void>()
  
  var buyTradesDataSource: BuySellTradesTableViewDataSource!
  var sellTradesDataSource: BuySellTradesTableViewDataSource!
  
  let headerView = HeaderView()
  
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
  
  let fab = FloatingActionButton()
  
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
                                       openTradesTableView,
                                       fab.view)
    customView.tapRecognizer.isEnabled = false
    
    buyTradesTableView.refreshControl = buyTradesRefreshControl
    sellTradesTableView.refreshControl = sellTradesRefreshControl
    
    setupFAB()
  }
  
  private func setupFAB() {
    fab.view.addItem(title: localize(L.Trades.create), image: UIImage(named: "fab_create")) { [unowned self] _ in
      self.didTapCreateRelay.accept(())
    }
    fab.view.addItem(title: localize(L.Trades.recall), image: UIImage(named: "fab_recall")) { [unowned self] _ in
      self.didTapRecallRelay.accept(())
    }
    fab.view.addItem(title: localize(L.Trades.reserve), image: UIImage(named: "fab_reserve")) { [unowned self] _ in
      self.didTapReserveRelay.accept(())
    }
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
    fab.view.snp.makeConstraints {
      $0.right.bottom.equalTo(view.safeAreaLayoutGuide).inset(16)
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
      .drive(onNext: { [headerView] coinBalance in
        let amountView = CryptoFiatAmountView()
        amountView.configure(for: coinBalance)
        
        let reservedAmountView = CryptoFiatAmountView()
        reservedAmountView.configure(for: coinBalance, useReserved: true)
        
        headerView.removeAll()
        headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withUSD)
        headerView.add(title: localize(L.CoinDetails.balance), valueView: amountView)
        headerView.add(title: localize(L.Trades.reserved), valueView: reservedAmountView)
      })
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
    let buyTradeSelectedDriver = buyTradesTableView.rx.itemSelected.asDriver()
    let sellTradeSelectedDriver = sellTradesTableView.rx.itemSelected.asDriver()
    let recallDriver = didTapRecallRelay.asDriver(onErrorDriveWith: .empty())
    let reserveDriver = didTapReserveRelay.asDriver(onErrorDriveWith: .empty())
    let createDriver = didTapCreateRelay.asDriver(onErrorDriveWith: .empty())
    
    presenter.bind(input: TradesPresenter.Input(back: backDriver,
                                                refreshBuyTrades: refreshBuyTradesDriver,
                                                refreshSellTrades: refreshSellTradesDriver,
                                                showMoreBuyTrades: showMoreBuyTradesDriver,
                                                showMoreSellTrades: showMoreSellTradesDriver,
                                                buyTradeSelected: buyTradeSelectedDriver,
                                                sellTradeSelected: sellTradeSelectedDriver,
                                                recall: recallDriver,
                                                reserve: reserveDriver,
                                                create: createDriver))
  }
  
  func tabBar(_ tabBar: MDCTabBar, didSelect item: UITabBarItem) {
    buyTradesTableView.isHidden = item.tag != buyTradesItem.tag
    sellTradesTableView.isHidden = item.tag != sellTradesItem.tag
    openTradesTableView.isHidden = item.tag != openTradesItem.tag
  }
}

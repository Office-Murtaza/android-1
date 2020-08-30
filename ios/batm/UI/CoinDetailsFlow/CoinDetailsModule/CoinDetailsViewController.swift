import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class CoinDetailsViewController: ModuleViewController<CoinDetailsPresenter> {
  
  let didTapDepositRelay = PublishRelay<Void>()
  let didTapWithdrawRelay = PublishRelay<Void>()
  let didTapSendGiftRelay = PublishRelay<Void>()
  let didTapSellRelay = PublishRelay<Void>()
  let didTapExchangeRelay = PublishRelay<Void>()
  let didTapTradesRelay = PublishRelay<Void>()
  let didTapStakingRelay = PublishRelay<Void>()
  
  let didSelectPeriodRelay = PublishRelay<SelectedPeriod>()
  
  var dataSource: CoinDetailsTableViewDataSource!
  
  let headerView = CoinDetailsHeaderView()
  
  let tableView = CoinDetailsTableView()
  
  let refreshControl = UIRefreshControl()
  
  let fab = FloatingActionButton()
  
  override var shouldShowNavigationBar: Bool { return true }
  
  override func viewWillAppear(_ animated: Bool) {
    if let index = self.tableView.indexPathForSelectedRow {
      self.tableView.deselectRow(at: index, animated: true)
    }
  }
  
  override func setupUI() {
    view.addSubviews(tableView,
                     fab.view)
    
    headerView.delegate = self
    
    tableView.refreshControl = refreshControl
    tableView.tableHeaderView = headerView
    
    headerView.topDivider.backgroundColor = tableView.separatorColor
    headerView.bottomDivider.backgroundColor = tableView.separatorColor
    
    setupFAB()
  }
  
  private func setupFAB() {
    fab.view.addItem(title: localize(L.CoinDetails.deposit), image: UIImage(named: "fab_deposit")) { [unowned self] _ in
      self.didTapDepositRelay.accept(())
    }
    fab.view.addItem(title: localize(L.CoinDetails.withdraw), image: UIImage(named: "fab_withdraw")) { [unowned self] _ in
      self.didTapWithdrawRelay.accept(())
    }
    fab.view.addItem(title: localize(L.CoinDetails.sendGift), image: UIImage(named: "fab_send_gift")) { [unowned self] _ in
      self.didTapSendGiftRelay.accept(())
    }
    
    // TODO: enable other actions when ready
    //    fab.view.addItem(title: localize(L.CoinDetails.sell), image: UIImage(named: "fab_sell")) { [unowned self] _ in
    //      self.didTapSellRelay.accept(())
    //    }
    //    fab.view.addItem(title: localize(L.CoinDetails.exchange), image: UIImage(named: "fab_exchange")) { [unowned self] _ in
    //      self.didTapExchangeRelay.accept(())
    //    }
    //    fab.view.addItem(title: localize(L.CoinDetails.trade), image: UIImage(named: "fab_trade")) { [unowned self] _ in
    //      self.didTapTradesRelay.accept(())
    //    }
  }
  
  override func setupLayout() {
    tableView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide)
      $0.left.right.bottom.equalToSuperview()
    }
    
    headerView.snp.makeConstraints {
      $0.centerX.width.equalTo(tableView)
      $0.top.equalTo(tableView)
    }
    headerView.layoutIfNeeded()
    tableView.tableHeaderView = headerView
    
    fab.view.snp.makeConstraints {
      $0.right.bottom.equalTo(view.safeAreaLayoutGuide).inset(16)
    }
  }
  
  func setupUIBindings() {
    tableView.dataSource = dataSource
    dataSource.tableView = tableView
    
    // TODO: enable staking when ready
    //    presenter.state
    //      .map { $0.coin?.type == .catm }
    //      .filter { $0 }
    //      .asObservable()
    //      .take(1)
    //      .subscribe(onNext: { [unowned self] _ in
    //        self.fab.view.addItem(title: localize(L.CoinDetails.staking), image: UIImage(named: "fab_staking")) { [unowned self] _ in
    //          self.didTapStakingRelay.accept(())
    //        }
    //      })
    //      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coinBalance }
      .filterNil()
      .drive(onNext: { [unowned self] in self.title = $0.type.verboseValue })
      .disposed(by: disposeBag)
    
    presenter.state
      .filter { $0.coinBalance != nil && $0.priceChartData != nil }
      .map { CoinDetailsHeaderViewConfig(coinBalance: $0.coinBalance!,
                                         priceChartData: $0.priceChartData!,
                                         selectedPeriod: $0.selectedPeriod) }
      .drive(onNext: { [headerView] in headerView.configure(with: $0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.transactions?.transactions.count ?? 0 }
      .map { $0 > 0 }
      .distinctUntilChanged()
      .drive(onNext: { [headerView, tableView] in
        if $0 {
          headerView.hideEmptyLabel()
          tableView.separatorStyle = .singleLine
        } else {
          headerView.showEmptyLabel()
          tableView.separatorStyle = .none
        }
        
        headerView.layoutIfNeeded()
        tableView.tableHeaderView = headerView
        tableView.layoutIfNeeded()
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.transactions?.transactions }
      .filterNil()
      .asObservable()
      .bind(to: dataSource.transactionsRelay)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.isFetching }
      .asObservable()
      .bind(to: refreshControl.rx.isRefreshing)
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let refreshDriver = refreshControl.rx.controlEvent(.valueChanged).asDriver()
    let depositDriver = didTapDepositRelay.asDriver(onErrorDriveWith: .empty())
    let withdrawDriver = didTapWithdrawRelay.asDriver(onErrorDriveWith: .empty())
    let sendGiftDriver = didTapSendGiftRelay.asDriver(onErrorDriveWith: .empty())
    let sellDriver = didTapSellRelay.asDriver(onErrorDriveWith: .empty())
    let exchangeDriver = didTapExchangeRelay.asDriver(onErrorDriveWith: .empty())
    let tradesDriver = didTapTradesRelay.asDriver(onErrorDriveWith: .empty())
    let stakingDriver = didTapStakingRelay.asDriver(onErrorDriveWith: .empty())
    let showMoreDriver = tableView.rx.willDisplayLastCell.asDriver(onErrorDriveWith: .empty())
    let transactionSelectedDriver = tableView.rx.itemSelected.asDriver()
    let updateSelectedPeriodDriver = didSelectPeriodRelay.asDriver(onErrorDriveWith: .empty())
    
    presenter.bind(input: CoinDetailsPresenter.Input(refresh: refreshDriver,
                                                     deposit: depositDriver,
                                                     withdraw: withdrawDriver,
                                                     sendGift: sendGiftDriver,
                                                     sell: sellDriver,
                                                     exchange: exchangeDriver,
                                                     trades: tradesDriver,
                                                     staking: stakingDriver,
                                                     showMore: showMoreDriver,
                                                     transactionSelected: transactionSelectedDriver,
                                                     updateSelectedPeriod: updateSelectedPeriodDriver))
  }
}

extension CoinDetailsViewController: CoinDetailsHeaderViewDelegate {
  
  func didSelectPeriod(_ period: SelectedPeriod) {
    didSelectPeriodRelay.accept(period)
  }
  
}

import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class CoinDetailsViewController: ModuleViewController<CoinDetailsPresenter> {
    var dataSource: CoinDetailsTableViewDataSource!
    
    let didTapDepositRelay = PublishRelay<Void>()
    let didTapWithdrawRelay = PublishRelay<Void>()
    let didTapSendGiftRelay = PublishRelay<Void>()
    let didTapSellRelay = PublishRelay<Void>()
    let didTapExchangeRelay = PublishRelay<Void>()
    let didTapTradesRelay = PublishRelay<Void>()
    let didTapRecallRelay = PublishRelay<Void>()
    let didTapReserveRelay = PublishRelay<Void>()
    let didSelectPeriodRelay = PublishRelay<SelectedPeriod>()
    
    lazy var headerView = CoinDetailsHeaderView()
    lazy var tableView = CoinDetailsTableView()
    lazy var refreshControl = UIRefreshControl()
    lazy var fab = FloatingActionButton()
    
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
        fab.view.addItem(title: localize(L.Trades.reserve), image: UIImage(named: "fab_reserve")) { [unowned self] _ in
            self.didTapReserveRelay.accept(())
        }
        fab.view.addItem(title: localize(L.Trades.recall), image: UIImage(named: "fab_recall")) { [unowned self] _ in
            self.didTapRecallRelay.accept(())
        }
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
        
        rx.firstTimeViewDidAppear
            .asObservable()
            .doOnNext { [weak self] in
                self?.presenter.setupTransactionDetailsNotification()
                self?.presenter.updateScreenRelay.accept(())
            }
            .subscribe()
            .disposed(by: disposeBag)
        
        presenter.state
            .map { $0.coinBalance }
            .filterNil()
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe { [unowned self] in
                self.title = $0.type.verboseValue
            }
            .disposed(by: disposeBag)
        
        presenter.state
            .filter { $0.coinBalance != nil }
            .asObservable()
            .map { CoinDetailsHeaderViewConfig(coinBalance: $0.coinBalance!,
                                               priceChartData: $0.priceChartDetails,
                                               selectedPeriod: $0.selectedPeriod,
                                               predefinedData: $0.predefinedData)}
            .subscribe { [headerView] in headerView.configure(with: $0) }
            .disposed(by: disposeBag)
        
        presenter.state
            .filter { $0.predefinedData != nil }
            .map {
                CoinDetailsHeaderViewConfig(coinBalance: ($0.predefinedData?.balance)!,
                                            priceChartData: $0.priceChartDetails,
                                            selectedPeriod: $0.selectedPeriod,
                                            predefinedData: $0.predefinedData)
                
            }
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
        let transactionSelectedDriver = tableView.rx.itemSelected.asDriver()
        let updateSelectedPeriodDriver = didSelectPeriodRelay.asDriver(onErrorDriveWith: .empty())
        let reserveDriver = didTapReserveRelay.asDriver(onErrorDriveWith: .empty())
        let recallDriver = didTapRecallRelay.asDriver(onErrorDriveWith: .empty())
        
        presenter.bind(input: CoinDetailsPresenter.Input(refresh: refreshDriver,
                                                         deposit: depositDriver,
                                                         withdraw: withdrawDriver,
                                                         sendGift: sendGiftDriver,
                                                         sell: sellDriver,
                                                         exchange: exchangeDriver,
                                                         trades: tradesDriver,
                                                         transactionSelected: transactionSelectedDriver,
                                                         updateSelectedPeriod: updateSelectedPeriodDriver,
                                                         recall: recallDriver,
                                                         reserve: reserveDriver))
    }
}

extension CoinDetailsViewController: CoinDetailsHeaderViewDelegate {
    func didSelectPeriod(_ period: SelectedPeriod) {
        didSelectPeriodRelay.accept(period)
    }
}

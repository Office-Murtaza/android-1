import UIKit
import RxSwift
import RxCocoa
import SnapKit

class WalletViewController: ModuleViewController<WalletPresenter> {
    
    var dataSource: WalletTableViewDataSource!
    
    let headerView = WalletHeaderView()
    
    let tableView = WalletTableView()
    
    let refreshControl = UIRefreshControl()
    
    override var shouldShowNavigationBar: Bool { return false }
    
    override func viewWillAppear(_ animated: Bool) {
        if let index = self.tableView.indexPathForSelectedRow {
            self.tableView.deselectRow(at: index, animated: true)
        }
        
        presenter.disconnectAndRemoveTransactionDetailsNotification()
        presenter.removeCoinDetails()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        guard let footerView = self.tableView.tableFooterView else { return }
        
        let width = self.tableView.bounds.size.width
        let size = footerView.systemLayoutSizeFitting(CGSize(width: width, height: UIView.layoutFittingCompressedSize.height))
        
        if footerView.frame.size.height != size.height {
            footerView.frame.size.height = size.height
            self.tableView.tableFooterView = footerView
        }
    }
    
    override func setupUI() {
        view.addSubviews(headerView,
                         tableView)
        
        headerView.divider.backgroundColor = tableView.separatorColor
        
        tableView.refreshControl = refreshControl
    }
    
    override func setupLayout() {
        headerView.snp.makeConstraints {
            $0.top.equalTo(view.safeAreaLayoutGuide)
            $0.left.right.equalToSuperview()
            $0.height.equalTo(125)
        }
        tableView.snp.makeConstraints {
            $0.top.equalTo(headerView.snp.bottom)
            $0.left.right.bottom.equalToSuperview()
        }
    }
    
    private func setupUIBindings() {
        tableView.dataSource = dataSource
        dataSource.tableView = tableView
        
        presenter.state
            .map { $0.coinsBalance }
            .drive(onNext: { [headerView] in headerView.configure(for: $0) })
            .disposed(by: disposeBag)
        
        presenter.state
            .map { $0.coins }
            .asObservable()
            .bind(to: dataSource.coinBalancesRelay)
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
        let coinSelectedDriver = tableView.rx.itemSelected.asDriver()
        
        presenter.bind(input: WalletPresenter.Input(refresh: refreshDriver,
                                                    coinSelected: coinSelectedDriver))
    }
}

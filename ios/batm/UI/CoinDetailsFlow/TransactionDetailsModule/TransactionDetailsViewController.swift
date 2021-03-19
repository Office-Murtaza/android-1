import UIKit
import RxCocoa
import RxSwift
import SnapKit
import GiphyUISDK
import GiphyCoreSDK
import MaterialComponents

final class TransactionDetailsViewController: ModuleViewController<TransactionDetailsPresenter> {
    var dataSource: TransactionDetailsDataSource?
    private lazy var tableView = TransactionDetailsTableView()
    
    override func setupUI() {
        title = localize(L.TransactionDetails.title)
        view.addSubview(tableView)
    }
    
    override func setupLayout() {
        tableView.snp.makeConstraints {
            $0.top.bottom.equalToSuperview()
            $0.left.equalToSuperview().offset(16)
            $0.right.equalToSuperview().offset(-16)
        }
    }
    
    func setupUIBindings() {
        tableView.dataSource = dataSource
        dataSource?.tableView = tableView
        
        rx.firstTimeViewDidAppear
            .asObservable()
            .doOnNext { [weak self] in
                self?.presenter.didViewLoadRelay.accept(()) }
            .subscribe()
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.coinType }
            .subscribe { [weak self] in
                self?.dataSource?.coinType = $0
            }
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.transactionDetails }
            .distinctUntilChanged()
            .filterNil()
            .subscribe(onNext: { [weak self] details in
                self?.dataSource?.transactionsRelay.accept(details)
            })
            .disposed(by: disposeBag)
    }
    
    override func setupBindings() {
        setupUIBindings()
    }
}

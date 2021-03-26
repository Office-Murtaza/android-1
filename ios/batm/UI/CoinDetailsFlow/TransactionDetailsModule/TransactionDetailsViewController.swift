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
        
        guard let dataSource = dataSource, let details = presenter.transactionDetails else { return }
        
        dataSource.transactionsRelay.accept(details)
    }
    
    override func setupBindings() {
        setupUIBindings()
        
        presenter.bind(input: TransactionDetailsPresenter.Input())
    }
}

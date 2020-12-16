import UIKit
import RxSwift
import RxCocoa
import SnapKit

class DealsViewController: ModuleViewController<DealsPresenter> {
    var dataSource: DealsTableViewDataSource!
    let tableView = DealsTableView()
    
    override func viewWillAppear(_ animated: Bool) {
        if let index = self.tableView.indexPathForSelectedRow {
            self.tableView.deselectRow(at: index, animated: true)
        }
    }
    
    override func setupUI() {
        view.backgroundColor = .white
        view.addSubviews(tableView)
    }
    
    override func setupLayout() {
        tableView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    }
    
    private func setupUIBindings() {
        dataSource.values = presenter.types
        tableView.dataSource = dataSource
        dataSource.tableView = tableView
    }
    
    override func setupBindings() {
        let selectDriver = tableView.rx.itemSelected.asDriver()
        
        setupUIBindings()
        presenter.bind(input: DealsPresenter.Input(select: selectDriver))
    }
    
}

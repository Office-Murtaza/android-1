import UIKit
import RxSwift
import RxCocoa
import SnapKit

class SecurityViewController: ModuleViewController<SecurityPresenter> {
  
  var dataSource: SettingsTableViewDataSource!
  
  let tableView = SettingsTableView()
  
  override func viewWillAppear(_ animated: Bool) {
    if let index = self.tableView.indexPathForSelectedRow {
      self.tableView.deselectRow(at: index, animated: true)
    }
  }
  
  override func setupUI() {
    title = localize(L.Security.title)
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
    setupUIBindings()
    
    let selectDriver = tableView.rx.itemSelected.asDriver()
    
    presenter.bind(input: SecurityPresenter.Input(select: selectDriver))
  }
  
}

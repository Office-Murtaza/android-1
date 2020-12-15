import UIKit
import RxSwift
import RxCocoa
import SnapKit

class ManageWalletsViewController: ModuleViewController<ManageWalletsPresenter> {
  
  var dataSource: ManageWalletsTableViewDataSource!
  
  let tableView = ManageWalletsTableView()
  
  override func setupUI() {
    title = localize(L.ManageWallets.title)
    
    view.addSubviews(tableView)
  }
  
  override func setupLayout() {
    tableView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide)
      $0.left.right.bottom.equalToSuperview()
    }
  }
  
  private func setupUIBindings() {
    tableView.dataSource = dataSource
    dataSource.tableView = tableView
    
    presenter.state
      .map { $0.coins.sorted { $0.index < $1.index } }
      .asObservable()
      .bind(to: dataSource.coinsRelay)
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let changeVisibilityDriver = dataSource.changeVisibilityRelay.asDriver(onErrorDriveWith: .empty())
    
    presenter.bind(input: ManageWalletsPresenter.Input(changeVisibility: changeVisibilityDriver))
  }
  
}

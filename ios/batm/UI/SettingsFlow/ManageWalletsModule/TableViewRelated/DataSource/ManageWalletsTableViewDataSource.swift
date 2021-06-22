import UIKit
import RxSwift
import RxCocoa

protocol ManageWalletsTableViewDataSourceDelegate: AnyObject {
  func changedVisibility(coin: BTMCoin, cell: ManageWalletsCell)
}

final class ManageWalletsTableViewDataSource: NSObject, UITableViewDataSource, HasDisposeBag {
  
  let coinsRelay = BehaviorRelay<[BTMCoin]>(value: [])
  weak var delegate: ManageWalletsTableViewDataSourceDelegate?
  
  private var values: [BTMCoin] = [] {
    didSet {
      values.sort { $0.index < $1.index }
      tableView?.reloadData()
    }
  }
  
  weak var tableView: UITableView? {
    didSet {
      guard let tableView = tableView else { return }
      tableView.register(ManageWalletsCell.self)
      tableView.reloadData()
    }
  }
  
  override init() {
    super.init()
    
    setupBindings()
  }
  
  private func setupBindings() {
    coinsRelay
      .subscribe(onNext: { [unowned self] in
                  self.values = $0
      })
      .disposed(by: disposeBag)
  }
  
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    values.count
  }
  
  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let model = values[indexPath.item]
    let cell = tableView.dequeueReusableCell(ManageWalletsCell.self, for: indexPath)
    cell.delegate = self
    cell.configure(for: model)
    return cell
  }
}

extension ManageWalletsTableViewDataSource: ManageWalletsCellDelegate {
  func didTapChangeVisibility(cell: ManageWalletsCell) {
    guard  let index = tableView?.indexPath(for: cell)?.row else { return }
    let coin = values[index]
    delegate?.changedVisibility(coin: coin, cell: cell)
  }
}

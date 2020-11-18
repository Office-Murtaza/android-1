import UIKit
import RxSwift
import RxCocoa

final class ManageWalletsTableViewDataSource: NSObject, UITableViewDataSource, HasDisposeBag {
  
  let coinsRelay = BehaviorRelay<[BTMCoin]>(value: [])
  let changeVisibilityRelay = PublishRelay<BTMCoin>()
  
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
      .subscribe(onNext: { [unowned self] in self.values = $0 })
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
  func didTapChangeVisibility(_ coin: BTMCoin) {
    changeVisibilityRelay.accept(coin)
  }
}

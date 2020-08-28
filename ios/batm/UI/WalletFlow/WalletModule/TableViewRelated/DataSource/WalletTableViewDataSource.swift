import UIKit
import RxSwift
import RxCocoa

final class WalletTableViewDataSource: NSObject, UITableViewDataSource, HasDisposeBag {
  
  let coinBalancesRelay = BehaviorRelay<[CoinBalance]>(value: [])
  
  private var values: [CoinBalance] = [] {
    didSet {
      tableView?.reloadData()
    }
  }
  
  weak var tableView: UITableView? {
    didSet {
      guard let tableView = tableView else { return }
      tableView.register(WalletCell.self)
      tableView.reloadData()
    }
  }
  
  override init() {
    super.init()
    
    setupBindings()
  }
  
  private func setupBindings() {
    coinBalancesRelay
      .subscribe(onNext: { [unowned self] in self.values = $0 })
      .disposed(by: disposeBag)
  }
  
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    values.count
  }
  
  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let model = values[indexPath.item]
    let cell = tableView.dequeueReusableCell(WalletCell.self, for: indexPath)
    cell.configure(for: model)
    return cell
  }
}

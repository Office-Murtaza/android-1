import UIKit
import RxSwift
import RxCocoa

final class CoinDetailsTableViewDataSource: NSObject, UITableViewDataSource, HasDisposeBag, ItemsCountProvider {
  
  let transactionsRelay = BehaviorRelay<[Transaction]>(value: [])
  
  private var values: [Transaction] = [] {
    didSet {
      tableView?.reloadData()
    }
  }
  
  weak var tableView: UITableView? {
    didSet {
      guard let tableView = tableView else { return }
      tableView.register(TransactionCell.self)
      tableView.reloadData()
    }
  }
  
  override init() {
    super.init()
    
    setupBindings()
  }
  
  private func setupBindings() {
    transactionsRelay
      .subscribe(onNext: { [unowned self] in self.values = $0 })
      .disposed(by: disposeBag)
  }
  
  func numberOfItems() -> Int {
    return values.count
  }
  
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return values.count
  }
  
  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let model = values[indexPath.item]
    let cell = tableView.dequeueReusableCell(TransactionCell.self, for: indexPath)
    cell.configure(for: model)
    return cell
  }
}

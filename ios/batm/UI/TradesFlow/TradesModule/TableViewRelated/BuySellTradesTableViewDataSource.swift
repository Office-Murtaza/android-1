import UIKit
import RxSwift
import RxCocoa

final class BuySellTradesTableViewDataSource: NSObject, UITableViewDataSource, HasDisposeBag, ItemsCountProvider {
  
  let tradesRelay = BehaviorRelay<[BuySellTrade]>(value: [])
  
  private var values: [BuySellTrade] = [] {
    didSet {
      tableView?.reloadData()
    }
  }
  
  weak var tableView: UITableView? {
    didSet {
      guard let tableView = tableView else { return }
      tableView.register(BuySellTradeCell.self)
      tableView.reloadData()
    }
  }
  
  override init() {
    super.init()
    
    setupBindings()
  }
  
  private func setupBindings() {
    tradesRelay
      .subscribe(onNext: { [unowned self] in self.values = $0 })
      .disposed(by: disposeBag)
  }
  
  func numberOfItems() -> Int {
    values.count
  }
  
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    values.count
  }
  
  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let model = values[indexPath.item]
    let cell = tableView.dequeueReusableCell(BuySellTradeCell.self, for: indexPath)
    cell.configure(for: model)
    return cell
  }
}

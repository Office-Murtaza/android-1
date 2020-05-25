import UIKit
import RxSwift

extension Reactive where Base: UITableView {
  var willDisplayLastCell: Observable<Void> {
    return willDisplayCell
      .filter { [weak base] _, indexPath  in
        guard let dataSource = base?.dataSource as? ItemsCountProvider else { return false }
        return indexPath.row == (dataSource.numberOfItems() - 1)
      }
      .debounce(1, scheduler: MainScheduler.instance)
      .map { _ in return () }
  }
}

import UIKit
import RxSwift

extension UITableView {
  func setEmptyMessage(_ message: String) {
      let messageLabel = UILabel(frame: CGRect(x: 0, y: 0, width: self.bounds.size.width, height: self.bounds.size.height))
      messageLabel.text = message
      messageLabel.textColor = .warmGrey
      messageLabel.textAlignment = .center
      messageLabel.font = .systemFont(ofSize: 16)
      messageLabel.numberOfLines = 0
      messageLabel.sizeToFit()

      self.backgroundView = messageLabel
  }

  func restore() {
      self.backgroundView = nil
  }
}

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

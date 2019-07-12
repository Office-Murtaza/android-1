import UIKit

protocol ReusableView {
  static var reuseIdentifier: String { get }
}

extension ReusableView {
  static var reuseIdentifier: String {
    return String(describing: self)
  }
}

extension UITableViewCell: ReusableView {}
extension UITableViewHeaderFooterView: ReusableView {}

extension UITableView {
  
  func register<T: UITableViewCell>(_ type: T.Type) {
    register(type, forCellReuseIdentifier: T.reuseIdentifier)
  }
  
  func registerHeaderFooterView<T: UITableViewHeaderFooterView>(_ type: T.Type) {
    register(type, forHeaderFooterViewReuseIdentifier: T.reuseIdentifier)
  }
  
  func dequeueReusableCell<T: UITableViewCell>(for indexPath: IndexPath) -> T {
    return dequeueReusableCell(T.self, for: indexPath)
  }
  
  func dequeueReusableCell<T: UITableViewCell>(_ type: T.Type, for indexPath: IndexPath) -> T {
    guard let cell = dequeueReusableCell(withIdentifier: T.reuseIdentifier, for: indexPath) as? T else {
      fatalError("Unable to dequeue TableViewCell: \(T.self) with identifier: \(T.reuseIdentifier)")
    }
    return cell
  }
  
  func dequeueReusableHeaderFooterView<T: UITableViewHeaderFooterView>(_ type: T.Type) -> T {
    guard let headerFooterView = dequeueReusableHeaderFooterView(withIdentifier: T.reuseIdentifier) as? T else {
      fatalError("Unable to dequeue TableViewHeaderFooterView: \(T.self) with identifier: \(T.reuseIdentifier)")
    }
    return headerFooterView
  }
}

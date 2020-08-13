import UIKit
import RxSwift
import RxCocoa

class SettingsTableView: UITableView, UITableViewDelegate {
  
  override init(frame: CGRect, style: UITableView.Style) {
    super.init(frame: frame, style: style)

    translatesAutoresizingMaskIntoConstraints = false
    backgroundColor = .white
    rowHeight = 55
    delegate = self
    separatorInset = .zero
    tableHeaderView = UIView()
    tableFooterView = UIView()
    bounces = false
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    
  }
}

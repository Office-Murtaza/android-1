import UIKit
import RxSwift
import RxCocoa

class TradesTableView: UITableView, UITableViewDelegate {
  
  override init(frame: CGRect, style: UITableView.Style) {
    super.init(frame: frame, style: style)

    translatesAutoresizingMaskIntoConstraints = false
    backgroundColor = .whiteTwo
    rowHeight = 60
    delegate = self
    separatorInset = .zero
    tableFooterView = UIView()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    
  }
}

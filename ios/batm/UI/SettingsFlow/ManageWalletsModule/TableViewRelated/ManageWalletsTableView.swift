import UIKit
import RxSwift
import RxCocoa

class ManageWalletsTableView: UITableView {
  
  override init(frame: CGRect, style: UITableView.Style) {
    super.init(frame: frame, style: style)

    translatesAutoresizingMaskIntoConstraints = false
    backgroundColor = .white
    rowHeight = 65
    separatorInset = .zero
    tableHeaderView = UIView()
    tableFooterView = UIView()
    allowsSelection = false
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}

import UIKit
import RxSwift
import RxCocoa

class WalletTableView: UITableView {
  
  override init(frame: CGRect, style: UITableView.Style) {
    super.init(frame: frame, style: style)

    translatesAutoresizingMaskIntoConstraints = false
    backgroundColor = .white
    rowHeight = 80
    separatorInset = .zero
    tableHeaderView = UIView()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}

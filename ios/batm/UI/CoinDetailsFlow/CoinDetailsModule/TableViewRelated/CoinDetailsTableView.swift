import UIKit
import RxSwift
import RxCocoa

class CoinDetailsTableView: UITableView {
  
  override init(frame: CGRect, style: UITableView.Style) {
    super.init(frame: frame, style: style)

    translatesAutoresizingMaskIntoConstraints = false
    backgroundColor = .white
    rowHeight = 50
    separatorInset = .zero
    tableFooterView = UIView()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}

import UIKit
import RxSwift
import RxCocoa

class DealsTableView: UITableView, UITableViewDelegate {
    lazy var headerView: UIView = {
        let line = UIView(frame: CGRect(x: 0,
                                        y: 0,
                                        width: self.frame.size.width,
                                        height: 1 / UIScreen.main.scale))
        line.backgroundColor = separatorColor
        return line
    }()
    
    override init(frame: CGRect, style: UITableView.Style) {
        super.init(frame: frame, style: style)
        
        translatesAutoresizingMaskIntoConstraints = false
        backgroundColor = .white
        rowHeight = 60
        delegate = self
        separatorInset = .zero
        tableHeaderView = headerView
        tableFooterView = UIView(frame: CGRect(x: 0, y: 0, width: frame.size.width, height: 1))
        bounces = false
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {}
}

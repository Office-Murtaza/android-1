import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents
import CoreLocation

enum P2PTradesType: Int {
    case buy=1
    case sell=2
}

protocol TradeListDataSource: UITableViewDataSource, UITableViewDelegate {
    func setup(trades: Trades, type: P2PTradesType)
    func reload(location: CLLocation?)
}

class TradeListViewController: UIViewController {
    
    let backgroundColor: UIColor
    
    lazy var tableView: UITableView = {
        let table = UITableView()
        table.separatorStyle = .none
        view.addSubview(table)
        return table
    }()
    
    init(color: UIColor) {
        backgroundColor = color
        super.init(nibName: nil, bundle: nil)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        view.backgroundColor = backgroundColor
    }

    
    func setupUI() {
        tableView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    }
    
}

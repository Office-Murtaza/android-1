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
    func setup(trades: Trades, type: P2PTradesType, userId: String?)
    func reload(location: CLLocation?)
}

protocol TradeListDelegate: AnyObject {
    func applyFilter(scope: FilterScopeModel)
    
    func resetFilter()
}

class TradeListViewController: UIViewController {
    
    weak var delegate: TradeListDelegate?
    
    private var type: P2PTradesType?
    private var location: CLLocation?
    
    lazy var tableView: UITableView = {
        let table = UITableView()
        table.separatorStyle = .none
        return table
    }()
    
    private let filterButton = P2PFloatingButton(image: UIImage(named: "p2p_filter_icon"))
    private var filterController: P2PFiltersViewController?
    
    
    init(type: P2PTradesType) {
        super.init(nibName: nil, bundle: nil)
        self.type = type
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func update(location: CLLocation?) {
        self.location = location
        filterController?.update(isLocationEnabled: (location != nil))
    }
    
    private func setupUI() {
        
        setupFilterController()
        
        filterButton.layer.cornerRadius = 28
        filterButton.layer.masksToBounds = false
        filterButton.layer.shadowColor = UIColor.black.cgColor
        filterButton.layer.shadowOpacity = 0.3
        filterButton.layer.shadowOffset = CGSize(width: 1, height: 2)
        filterButton.delegate = self
        
        view.addSubviews([
            tableView,
            filterButton
        ])
        
    }
    
    private func setupFilterController() {
        let sortBy: P2PFilterSortType = .price
        
        filterController = P2PFiltersViewController(coins: CustomCoinType.allCases,
                                                    payments: TradePaymentMethods.allCases,
                                                    sortTypes: P2PFilterSortType.allCases,
                                                    preselectedSortBy: sortBy,
                                                    minRange: 0,
                                                    maxRange: 10)
        filterController?.delegate = self
    }
    
    private func setupLayout() {
        tableView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    
        filterButton.snp.makeConstraints {
            $0.width.height.equalTo(56)
            $0.right.equalToSuperview().offset(-18)
            $0.bottom.equalToSuperview().offset(-18)
        }
        
    }
    
}

extension TradeListViewController: P2PFloatingButtonDelegate {
    func didTapFloadingButton() {
        guard let filter = filterController else { return }
        present(filter, animated: true, completion: nil)
    }
}

extension TradeListViewController: P2PFiltersViewControllerDelegate {
    func applyFilter(scope: FilterScopeModel) {
        delegate?.applyFilter(scope: scope)
        dismiss(animated: true, completion: nil)
    }
    
    func resetAllFilters() {
        delegate?.resetFilter()
        dismiss(animated: true, completion: nil)
    }
}


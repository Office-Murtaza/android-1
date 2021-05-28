import UIKit
import SnapKit


enum P2PFilterSortType: CaseIterable {
    case price
    case distance
    
    var title: String {
        switch self {
        case .price: return localize(L.P2p.Filter.Price.title)
        case .distance: return localize(L.P2p.Filter.Range.distance)
        }
    }
    
    init?(type: String) {
        switch type {
        case P2PFilterSortType.price.title: self = .price
        case P2PFilterSortType.distance.title: self = .distance
        default: return nil
        }
    }
}

protocol P2PFiltersViewControllerDelegate: AnyObject {
    func applyFilter(scope: FilterScopeModel)
    func resetAllFilters()
}

struct FilterScopeModel {
    let coins: [CustomCoinType]
    let paymentMethods: [TradePaymentMethods]
    let sortType: [P2PFilterSortType]
    let minRange: Double
    let maxRange: Double
    
    var isEmpty: Bool {
        return coins.isEmpty && paymentMethods.isEmpty && sortType.isEmpty
    }
}

class P2PFiltersViewController: UIViewController {

    weak var delegate: P2PFiltersViewControllerDelegate?
    
    private let scrollView = UIScrollView()

    private lazy var stackView: UIStackView = {
        let stack = UIStackView()
        stack.axis = .vertical
        return stack
    }()
    
    private let coinsHeader = P2PSectionHeaderView()
    private let coinsTagsView = P2PTagContainerView(width: UIScreen.main.bounds.size.width - 20)
    
    private let coinsSeparator = P2PSeparatorView()
    
    private let paymentMethodsHeader = P2PSectionHeaderView()
    private let paymentMethodsView = P2PTagContainerView(width: UIScreen.main.bounds.size.width - 20)
    
    private let paymentMethodSeparator = P2PSeparatorView()
    
    private let distanceHeader = P2PSectionHeaderView()
    private let distanceView = P2PDistanceRangeView()
    
    private let sortBySeparator = P2PSeparatorView()
    private let sortByHeader = P2PSectionHeaderView()
    private let sortByTagsView = P2PTagContainerView(width: UIScreen.main.bounds.size.width - 20)
    
    private let resetAllButton = P2PResetAllView()
    
    private var minRange:Double?
    private var maxRange:Double?
    private var defaultMinRange:Double?
    private var defaultMaxRange:Double?
    
    private var coins: [CustomCoinType]
    private var payments: [TradePaymentMethods]
    private var sortType: [P2PFilterSortType]
    private var preselectedSortBy: P2PFilterSortType
    private var isDistancePresentationEnabled: Bool = false
    
    init(coins: [CustomCoinType],
         payments: [TradePaymentMethods],
         sortTypes: [P2PFilterSortType],
         preselectedSortBy: P2PFilterSortType,
         minRange: Double,
         maxRange: Double) {
        self.coins = coins
        self.payments = payments
        self.sortType = sortTypes
        self.minRange = minRange
        self.maxRange = maxRange
        self.defaultMinRange = minRange
        self.defaultMaxRange = maxRange
        self.preselectedSortBy = preselectedSortBy
        super.init(nibName: nil, bundle: nil)
    }
    
    func update(isLocationEnabled: Bool) {
        isDistancePresentationEnabled = isLocationEnabled
        distanceView.isHidden = !isDistancePresentationEnabled
        distanceHeader.isHidden = !isDistancePresentationEnabled
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func hideKeyboard() {
        view.endEditing(true)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupLayout()
        
        setupCoinsView(coins: CustomCoinType.allCases)
      coinsHeader.update(title: localize(L.P2p.Filter.Coins.title))
        
      paymentMethodsHeader.update(title: localize(L.P2p.Filter.Payment.Methods.title))
        setupPaymentMethodsView(payments: payments)
        
      distanceHeader.update(title: localize(L.P2p.Filter.Distance.title))
        
      distanceView.setup(range: [CGFloat(minRange ?? 0), CGFloat(maxRange ?? 0)], measureString: "", stepSize: 1)
      distanceView.update(isUserInteractionEnabled: true, keyboardType: .decimalPad)
        
      sortByHeader.update(title: localize(L.P2p.Filter.Sort.By.title))
        setupSortByView(tags: sortType.map{ $0.title })
        
        
        resetAllButton.didTapApplyFilter { [weak self] in
            self?.applyFilter()
        } didTapReset: { [weak self] in
            self?.resetAll()
        }
        
        distanceView.selectedMinRange { [weak self] minRange in
            self?.minRange = minRange
        } maxRange: { [weak self] maxRange in
            self?.maxRange = maxRange
        }

        let tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(hideKeyboard))
        view.addGestureRecognizer(tapRecognizer)
        setupDefaultKeyboardHandling()
    }

    private func setupUI() {
        view.backgroundColor = .white
        
        view.addSubviews([
            scrollView,
        ])
        
        scrollView.addSubview(stackView)
        
        stackView.addArrangedSubviews([
            coinsHeader,
            coinsTagsView,
            coinsSeparator,
            paymentMethodsHeader,
            paymentMethodsView,
            paymentMethodSeparator,
            distanceHeader,
            distanceView,
            sortBySeparator,
            sortByHeader,
            sortByTagsView,
            resetAllButton
        ])
        
        distanceView.isHidden = !isDistancePresentationEnabled
        distanceHeader.isHidden = !isDistancePresentationEnabled
    }
    
    private func setupLayout() {
        
        let separatorHeight = 1 / UIScreen.main.scale
        
        scrollView.snp.makeConstraints {
            $0.centerX.equalTo(view.snp.centerX)
            $0.width.equalToSuperview()
            $0.top.equalToSuperview()
            $0.bottom.equalToSuperview()
        }
        
        stackView.snp.makeConstraints {
            $0.top.equalToSuperview()
            $0.bottom.equalToSuperview()
            $0.right.equalToSuperview()
            $0.left.equalToSuperview()
            $0.width.equalToSuperview()
        }
        
        coinsHeader.snp.makeConstraints {
            $0.top.right.left.equalToSuperview()
            $0.height.equalTo(65)
        }
        
        coinsTagsView.snp.makeConstraints {
            $0.top.equalTo(coinsHeader.snp.bottom)
            $0.left.equalToSuperview().offset(10)
            $0.right.equalToSuperview().offset(-10)
        }
        
        coinsSeparator.snp.makeConstraints {
            $0.top.equalTo(coinsTagsView.snp.bottom)
            $0.height.equalTo(separatorHeight)
            $0.left.right.equalToSuperview()
        }
        
        paymentMethodsHeader.snp.remakeConstraints {
            $0.top.equalTo(coinsSeparator.snp.bottom)
            $0.right.left.equalToSuperview()
            $0.height.equalTo(65)
        }
        
        paymentMethodsView.snp.makeConstraints {
            $0.top.equalTo(paymentMethodsHeader.snp.bottom)
            $0.right.left.equalToSuperview()
        }
        
        paymentMethodSeparator.snp.makeConstraints {
            $0.top.equalTo(paymentMethodsView.snp.bottom)
            $0.height.equalTo(separatorHeight)
            $0.left.right.equalToSuperview()
        }
        
        distanceHeader.snp.makeConstraints {
            $0.top.equalTo(paymentMethodSeparator.snp.bottom)
            $0.right.left.equalToSuperview()
            $0.height.equalTo(65)
        }
        
        distanceView.snp.makeConstraints {
            $0.left.equalToSuperview().offset(30)
            $0.right.equalToSuperview().offset(-30)
            $0.top.equalTo(distanceHeader.snp.bottom)
            $0.height.equalTo(100)
        }
        
        sortBySeparator.snp.makeConstraints {
            $0.top.equalTo(distanceView.snp.bottom)
            $0.height.equalTo(separatorHeight)
            $0.left.right.equalToSuperview()
        }
        
        sortByHeader.snp.makeConstraints {
            $0.top.equalTo(sortBySeparator.snp.bottom)
            $0.right.left.equalToSuperview()
            $0.height.equalTo(65)
        }
        
        sortByTagsView.snp.makeConstraints {
            $0.top.equalTo(sortByHeader.snp.bottom)
            $0.left.equalToSuperview().offset(10)
            $0.right.equalToSuperview().offset(-10)
        }
        
        resetAllButton.snp.makeConstraints {
            $0.top.equalTo(sortByTagsView.snp.bottom)
            $0.left.right.equalToSuperview()
            $0.height.equalTo(89)
        }
    }
    
    private func setupCoinsView(coins: [CustomCoinType]) {
        var tags = [P2PTagView]()
        
        for type in coins {
            let tag = P2PTagView()
            if type.defaultCoinType == .bitcoin {
                tag.didSelected()
            }
            tag.update(image: type.mediumLogo, title: type.code)
            tag.layoutIfNeeded()
            tags.append(tag)
        }
        
        coinsTagsView.update(tags: tags)
    }
    
    private func setupPaymentMethodsView(payments: [TradePaymentMethods]) {
        var methods = [P2PTagView]()
        for method in payments {
            let tag = P2PTagView()
            tag.didSelected()
            tag.update(image: method.image, title: method.title)
            tag.layoutIfNeeded()
            methods.append(tag)
        }
        
        paymentMethodsView.update(tags: methods)
    }

    private func setupSortByView(tags: [String]) {
        var tagViews = [P2PTagView]()
        for title in tags {
            let tag = P2PTagView()
            if title == preselectedSortBy.title {
                tag.didSelected()
            }
            tag.delegte = self
            tag.update(image: nil, title: title)
            tag.layoutIfNeeded()
            tagViews.append(tag)
        }
        
        sortByTagsView.update(tags: tagViews)
    }
    
    private func applyFilter() {
        let selectedCoins = coinsTagsView.selectedTitles().compactMap { CustomCoinType(code: $0) }
        let selectedPayments = paymentMethodsView.selectedTitles().compactMap { TradePaymentMethods(method: $0) }
        let soretedTypes = sortByTagsView.selectedTitles().compactMap { P2PFilterSortType(type: $0) }
    
        
        let scope = FilterScopeModel(coins: selectedCoins,
                                     paymentMethods: selectedPayments,
                                     sortType: soretedTypes,
                                     minRange: minRange ?? 0,
                                     maxRange: maxRange ?? 0)
        
        self.delegate?.applyFilter(scope: scope)
    }
    
    private func resetAll() {
        coinsTagsView.resetAll()
        coinsTagsView.selectTag(title: CustomCoinType.bitcoin.code)
        
        paymentMethodsView.resetAll()
        paymentMethodsView.selectAll()
        
        sortByTagsView.resetAll()
        sortByTagsView.selectTag(title: P2PFilterSortType.price.title)
        distanceView.setup(range: [CGFloat(defaultMinRange ?? 0), CGFloat(defaultMaxRange ?? 0)], measureString: "", stepSize: 1)
        
        self.delegate?.resetAllFilters()
    }
    
}

extension P2PFiltersViewController: P2PTagViewDelegate {
    func didTapTag(view: P2PTagView) {
        sortByTagsView.resetAll()
        view.didSelected()
    }
}

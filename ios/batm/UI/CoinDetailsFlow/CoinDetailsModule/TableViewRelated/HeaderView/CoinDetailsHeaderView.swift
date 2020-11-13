import UIKit

protocol CoinDetailsHeaderViewDelegate: class {
  func didSelectPeriod(_ period: SelectedPeriod)
}

struct CoinDetailsHeaderViewConfig {
  let coinBalance: CoinBalance
  let priceChartData: PriceChartData
  let selectedPeriod: SelectedPeriod
}

class CoinDetailsHeaderView: UICollectionReusableView, HasDisposeBag {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 30
    return stackView
  }()
  
  let contentView = UIView()
  
  let chartView = CoinDetailsChartView()
  
  let topDivider = UIView()
  
  let balanceView = CoinDetailsBalanceView()
  
  let bottomDivider = UIView()
  
  let emptyLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinDetails.Transactions.empty)
    label.textColor = .warmGrey
    label.textAlignment = .center
    label.font = .systemFont(ofSize: 16)
    label.numberOfLines = 0
    return label
  }()
  
  weak var delegate: CoinDetailsHeaderViewDelegate?
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
    setupBindings()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubview(stackView)
    stackView.addArrangedSubviews(contentView,
                                  emptyLabel)
    contentView.addSubviews(chartView,
                            topDivider,
                            balanceView,
                            bottomDivider)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    chartView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(320)
    }
    topDivider.snp.makeConstraints {
      $0.top.equalTo(chartView.snp.bottom)
      $0.left.right.equalToSuperview()
      $0.height.equalTo(1 / UIScreen.main.scale)
    }
    balanceView.snp.makeConstraints {
      $0.top.equalTo(topDivider.snp.bottom)
      $0.left.right.equalToSuperview()
      $0.height.equalTo(80)
    }
    bottomDivider.snp.makeConstraints {
      $0.top.equalTo(balanceView.snp.bottom)
      $0.left.right.bottom.equalToSuperview()
      $0.height.equalTo(1 / UIScreen.main.scale)
    }
  }
  
  private func setupBindings() {
    chartView.periodButtonsView.rx.selectedPeriod
      .drive(onNext: { [unowned self] in self.delegate?.didSelectPeriod($0) })
      .disposed(by: disposeBag)
  }
  
  func showEmptyLabel() {
    if emptyLabel.superview == nil {
      stackView.addArrangedSubview(emptyLabel)
    }
  }
  
  func hideEmptyLabel() {
    if emptyLabel.superview != nil {
      emptyLabel.removeFromSuperview()
    }
  }
  
  func configure(with config: CoinDetailsHeaderViewConfig) {
    chartView.configure(for: config.priceChartData, and: config.selectedPeriod)
    balanceView.configure(for: config.coinBalance)
  }
  
}

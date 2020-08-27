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
  
  let chartView = CoinDetailsChartView()
  
  let topDivider = UIView()
  
  let balanceView = CoinDetailsBalanceView()
  
  let bottomDivider = UIView()
  
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
    
    addSubviews(chartView,
                topDivider,
                balanceView,
                bottomDivider)
  }
  
  private func setupLayout() {
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
      $0.height.equalTo(50)
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
  
  func configure(with config: CoinDetailsHeaderViewConfig) {
    chartView.configure(for: config.priceChartData, and: config.selectedPeriod)
    balanceView.configure(for: config.coinBalance)
  }
  
}

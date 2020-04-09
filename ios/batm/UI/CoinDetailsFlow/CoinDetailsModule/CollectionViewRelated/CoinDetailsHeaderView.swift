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
    
    addSubview(chartView)
  }
  
  private func setupLayout() {
    chartView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  private func setupBindings() {
    chartView.periodButtonsView.rx.selectedPeriod
      .drive(onNext: { [unowned self] in self.delegate?.didSelectPeriod($0) })
      .disposed(by: disposeBag)
  }
  
  func configure(with config: CoinDetailsHeaderViewConfig) {
    chartView.configure(for: config.priceChartData, and: config.selectedPeriod)
    chartView.balanceView.configure(for: config.coinBalance)
  }
  
}

import UIKit

protocol CoinDetailsHeaderViewDelegate: class {
  func didTapDeposit()
  func didTapWithdraw()
  func didTapSendGift()
  func didTapSell()
  func didSelectPeriod(_ period: SelectedPeriod)
}

struct CoinDetailsHeaderViewConfig {
  let coinBalance: CoinBalance
  let priceChartData: PriceChartData
  let selectedPeriod: SelectedPeriod
}

class CoinDetailsHeaderView: UICollectionReusableView, HasDisposeBag {
  
  let chartView = CoinDetailsChartView()
  let balanceView = CoinDetailsBalanceView()
  let buttonsView = CoinDetailsButtonsView()
  
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
                balanceView,
                buttonsView)
  }
  
  private func setupLayout() {
    chartView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    balanceView.snp.makeConstraints {
      $0.top.equalTo(chartView.snp.bottom).offset(20)
      $0.centerX.equalToSuperview()
      $0.left.greaterThanOrEqualToSuperview().offset(10)
      $0.right.lessThanOrEqualToSuperview().offset(-10)
    }
    buttonsView.snp.makeConstraints {
      $0.top.equalTo(balanceView.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(10)
      $0.bottom.equalToSuperview()
    }
  }
  
  private func setupBindings() {
    buttonsView.rx.depositTap
      .drive(onNext: { [unowned self] in self.delegate?.didTapDeposit() })
      .disposed(by: disposeBag)
    
    buttonsView.rx.withdrawTap
      .drive(onNext: { [unowned self] in self.delegate?.didTapWithdraw() })
      .disposed(by: disposeBag)
    
    buttonsView.rx.sendGiftTap
      .drive(onNext: { [unowned self] in self.delegate?.didTapSendGift() })
      .disposed(by: disposeBag)
    
    buttonsView.rx.sellTap
      .drive(onNext: { [unowned self] in self.delegate?.didTapSell() })
      .disposed(by: disposeBag)
    
    chartView.periodButtonsView.rx.selectedPeriod
      .drive(onNext: { [unowned self] in self.delegate?.didSelectPeriod($0) })
      .disposed(by: disposeBag)
  }
  
  func configure(with config: CoinDetailsHeaderViewConfig) {
    chartView.configure(for: config.priceChartData, and: config.selectedPeriod)
    balanceView.configure(for: config.coinBalance)
  }
  
}

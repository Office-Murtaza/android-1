import UIKit
import Charts

class CoinDetailsChartView: UIView {
  
  let priceLabel: UILabel = {
    let label = UILabel()
    label.textColor = .ceruleanBlue
    label.font = .systemFont(ofSize: 22, weight: .bold)
    return label
  }()
  
  let changeRateContainer = UIView()
  
  let changeRateImageView = UIImageView(image: nil)
  
  let changeRateLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 14)
    return label
  }()
  
  let chartView: LineChartView = {
    let view = LineChartView()
    view.dragEnabled = false
    view.setScaleEnabled(false)
    view.autoScaleMinMaxEnabled = true
    view.highlightPerTapEnabled = false
    view.xAxis.enabled = false
    view.leftAxis.enabled = false
    view.rightAxis.enabled = false
    view.legend.enabled = false
    view.minOffset = 0
    view.setExtraOffsets(left: 0, top: 5, right: 0, bottom: 5)
    view.clipDataToContentEnabled = false
    return view
  }()
  
  let periodButtonsView = CoinDetailsPeriodButtonsView()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    backgroundColor = .white
    
    addSubviews(chartView,
                priceLabel,
                changeRateContainer,
                periodButtonsView)
    changeRateContainer.addSubviews(changeRateImageView,
                                    changeRateLabel)
  }
  
  private func setupLayout() {
    priceLabel.snp.makeConstraints {
      $0.top.equalToSuperview().offset(25)
      $0.centerX.equalToSuperview()
    }
    changeRateContainer.snp.makeConstraints {
      $0.top.equalTo(priceLabel.snp.bottom).offset(5)
      $0.centerX.equalToSuperview()
    }
    changeRateImageView.snp.makeConstraints {
      $0.left.centerY.equalToSuperview()
    }
    changeRateLabel.snp.makeConstraints {
      $0.left.equalTo(changeRateImageView.snp.right).offset(7)
      $0.top.right.bottom.equalToSuperview()
    }
    chartView.snp.makeConstraints {
      $0.top.equalTo(changeRateContainer.snp.bottom).offset(35)
      $0.left.right.equalToSuperview()
    }
    periodButtonsView.snp.makeConstraints {
      $0.top.equalTo(chartView.snp.bottom).offset(40)
      $0.centerX.equalToSuperview()
      $0.bottom.equalToSuperview().offset(-15)
    }
  }
  
  func configure(for data: PriceChartDetails, and selectedPeriod: SelectedPeriod, balance: CoinBalance) {

    let period: [[Double]]
    switch selectedPeriod {
    case .oneDay: period = data.prices
    case .oneWeek: period = data.prices
    case .oneMonth: period = data.prices
    case .threeMonths: period = data.prices
    case .oneYear: period = data.prices
    }
    
    priceLabel.text = balance.fiatBalance.fiatFormatted.withDollarSign
    
    guard let firstPrice = data.prices.first?.last,
    let lastPrice = data.prices.last?.last else { return }
    let rate = (lastPrice - firstPrice) / firstPrice * 100
    
    changeRateImageView.image = rate < 0
      ? UIImage(named: "change_rate_down")
      : UIImage(named: "change_rate_up")
    changeRateLabel.text = "\(abs(rate).formatted(fractionPart: 2)) %"
    changeRateLabel.textColor = rate < 0 ? .tomato : .darkMint
    
    configureChart(with: period)
    periodButtonsView.configure(for: selectedPeriod)
  }

  private func configureChart(with prices: [[Double]]) {
    let entries = prices.enumerated().map { (element) -> ChartDataEntry? in
      guard let first = element.element.first, let second = element.element.last else { return nil }
      return ChartDataEntry(x: first, y: second)
    }
    let dataSet = LineChartDataSet(entries: entries.compactMap{ $0 })
    dataSet.mode = .cubicBezier
    dataSet.drawValuesEnabled = false
    dataSet.drawCirclesEnabled = false
    dataSet.setColor(.skyBlue)
    
    chartView.data = LineChartData(dataSet: dataSet)
  }
  
  
}


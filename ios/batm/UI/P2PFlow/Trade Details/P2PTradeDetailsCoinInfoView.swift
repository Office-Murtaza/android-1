import UIKit


struct P2PTradeDetailsCoinInfoModel {
  let trade: Trade
  
  var coin: CustomCoinType? {
      return CustomCoinType(code:trade.coin ?? "BTC")
  }
  
  var price: String {
      return "$ \((trade.price ?? 0).coinFormatted)"
  }
  
  var limit: String {
      return "$ \((trade.minLimit ?? 0).coinFormatted) - $ \((trade.maxLimit ?? 0).coinFormatted)"
  }
  
  var sellbuyType: P2PSellBuyViewType {
      return trade.type == 1 ? .buy : .sell
  }
  
}

class P2PTradeDetailsCoinInfoView: UIView {

  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
    setupLayout()
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private let coinView = P2PCoinView()
  
  private let sellBuyView = P2PSellBuyView()
  
  private lazy var priceLabel: UILabel = {
      let price = UILabel()
      price.font = .systemFont(ofSize: 16, weight: .regular)
      return price
  }()
  
  private lazy var limitLabel: UILabel = {
      let label = UILabel()
      label.font = .systemFont(ofSize: 16, weight: .bold)
      return label
  }()
  
  private lazy var separatorView: UIView = {
      let view = UIView()
      view.backgroundColor = .gray
      return view
  }()
  
  private func setupUI() {
      addSubviews([
          coinView,
          priceLabel,
          limitLabel,
          separatorView,
          sellBuyView
      ])
  }
  
  private func setupLayout() {
      coinView.snp.makeConstraints {
          $0.top.equalToSuperview().offset(16)
          $0.left.equalToSuperview().offset(16)
          $0.height.equalTo(24)
      }
      
      priceLabel.snp.makeConstraints {
          $0.top.equalTo(coinView.snp.bottom).offset(5)
          $0.left.equalTo(coinView)
      }
      
      limitLabel.snp.makeConstraints {
          $0.top.equalTo(coinView)
          $0.right.equalToSuperview().offset(-16)
      }
      
      let separatorHeight = 1 / UIScreen.main.scale
      separatorView.snp.makeConstraints {
          $0.height.equalTo(separatorHeight)
          $0.bottom.equalToSuperview()
          $0.left.equalToSuperview().offset(16)
          $0.right.equalToSuperview().offset(-16)
      }
      
      sellBuyView.snp.makeConstraints {
          $0.top.equalTo(coinView.snp.top)
          $0.left.equalTo(coinView.snp.right).offset(10)
      }
  }
  
  func update(data: P2PTradeDetailsCoinInfoModel) {
      priceLabel.text = data.price
      coinView.update(coin: data.coin)
      limitLabel.text = data.limit
      sellBuyView.update(type: data.sellbuyType)
  }
  
}

import UIKit

class P2PTradeDetailsBaseViewController: UIViewController {
  
  var trade: Trade?
  private let scrollView = UIScrollView()
  private let coinInfoView = P2PTradeDetailsCoinInfoView()

  private let paymentMethods = P2PTradeDetailsPaymentMethodsView()
  let paymentMethodsSeparator = P2PSeparatorView()
  
  lazy var stackView: UIStackView = {
    let stack = UIStackView()
    stack.axis = .vertical
    return stack
  }()
  
  override func viewDidLoad() {
    super.viewDidLoad()
    view.backgroundColor = .white
    setupUI()
    setupLayout()
  }

  func setup(trade: Trade) {
    self.trade = trade
    let infoModel = P2PTradeDetailsCoinInfoModel(trade: trade)
    coinInfoView.update(data: infoModel)
    setupPaymenMethodsView(trade: trade)
  }
  
  func setupPaymenMethodsView(trade: Trade) {
    guard let methods = trade.paymentMethods else { return }
    let images =  methods
        .components(separatedBy: ",")
        .compactMap{ Int($0) }
        .compactMap{ TradePaymentMethods.init(rawValue: $0)?.image }
    
    paymentMethods.update(images: images)
  }
  
  func setupUI() {
    view.addSubviews([
      scrollView,
    ])
    
    scrollView.addSubview(stackView)
    stackView.addArrangedSubviews([
      coinInfoView,
      paymentMethods,
      paymentMethodsSeparator,
    ])
  }
  
  func setupLayout() {
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
    
    coinInfoView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(105)
    }
    
    paymentMethods.snp.makeConstraints {
      $0.top.equalTo(coinInfoView.snp.bottom)
      $0.left.right.equalToSuperview()
      $0.height.equalTo(56)
    }
    
    paymentMethodsSeparator.snp.makeConstraints {
        $0.top.equalTo(paymentMethods.snp.bottom)
        $0.height.equalTo(separatorHeight)
        $0.left.equalToSuperview().offset(15)
        $0.right.equalToSuperview().offset(-15)
    }
  }
  
}
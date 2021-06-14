import UIKit
import SnapKit
import MaterialComponents

class P2PTradeDetailsBuyViewController: P2PTradeDetailsBaseViewController {
  
  weak var delegate: P2PTradeDetailsCreateOrderDelegate?
    
  private let tradeView =  P2PTradeDetailsRateView()
  private let tradeViewSeparator = P2PSeparatorView()
  private let infoMessageView = P2PTradeDetailsTextInfoView()
  private let buyButton = MDCButton.buy
  private var reservedBalance: Double = 0
  private var platformFee: Double = 0
  
  func setup(trade: Trade, distance: String, reservedBalance: Double, platformFee: Double) {
    super.setup(trade: trade)
    self.platformFee = platformFee
    self.reservedBalance = reservedBalance
    guard let makerId = trade.makerPublicId,
          let tradeRate = trade.makerTradingRate,
          let totalTrades = trade.makerTotalTrades else { return }
    tradeView.setup(markerId: makerId , statusImage: nil, rate: tradeRate, totalTrades: totalTrades, distance: distance)
  }
    
  override func setupUI() {
    super.setupUI()
    
    tradeView.delegate = self
    
    stackView.addArrangedSubviews([
      tradeView,
      tradeViewSeparator,
      infoMessageView,
      buyButton
    ])
    
    infoMessageView.update(message: localize(L.P2p.Trade.Details.info))
    
    buyButton.addTarget(self, action: #selector(buyTrade), for: .touchUpInside)
  
  }
  
  @objc func buyTrade() {
    guard let trade = trade else { return }
    let controller = P2PCreateOrderPopupViewController()
    controller.delegate = self
    controller.setup(trade: trade,
                     platformFee: platformFee,
                     reservedBalance: reservedBalance,
                     type: .buy)
    
    controller.modalPresentationStyle = .overCurrentContext
    present(controller, animated: true, completion: nil)
  }
  
  override func setupLayout() {
    super.setupLayout()
    
    let separatorHeight = 1 / UIScreen.main.scale
    
    tradeView.snp.makeConstraints {
      $0.top.equalTo(paymentMethodsSeparator.snp.bottom)
      $0.left.right.equalToSuperview()
      $0.height.equalTo(70)
    }
    
    tradeViewSeparator.snp.makeConstraints {
      $0.top.equalTo(tradeView.snp.bottom)
      $0.height.equalTo(separatorHeight)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
    }
    
    infoMessageView.snp.makeConstraints {
      $0.top.equalTo(tradeViewSeparator.snp.bottom)
      $0.left.right.equalToSuperview()
    }
    
    buyButton.snp.makeConstraints {
      $0.top.equalTo(infoMessageView.snp.bottom)
      $0.right.equalToSuperview().offset(-15)
      $0.left.equalToSuperview().offset(15)
      $0.height.equalTo(50)
    }
  }
}

extension P2PTradeDetailsBuyViewController: P2PCreateOrderPopupViewControllerDelegate {
    func createOrder(price: Double, cryptoAmount: Double, fiatAmount: Double) {
        guard let trade = trade, let tradeId = trade.id else { return }
        let model = P2PCreateOrderDataModel(
            tradeId: tradeId,
            price: price,
            cryptoAmount: cryptoAmount,
            fiatAmount: fiatAmount)
        delegate?.createOrder(model: model)
    }
}

extension P2PTradeDetailsBuyViewController: P2PTradeDetailsRateViewDelegate {
    func didTapDistance() {
        guard let trade = trade else { return }
        delegate?.didTapDistance(trade: trade)
    }
}

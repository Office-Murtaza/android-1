import UIKit
import SnapKit
import MaterialComponents

class P2PTradeDetailsBuyViewController: P2PTradeDetailsBaseViewController {
  private let tradeView =  P2PTradeDetailsRateView()
  private let tradeViewSeparator = P2PSeparatorView()
  
  private let infoMessageView = P2PTradeDetailsTextInfoView()
  
  private let buyButton = MDCButton.buy
  
  
  func setup(trade: Trade, distance: String) {
    super.setup(trade: trade)
    guard let makerId = trade.makerPublicId,
          let tradeRate = trade.makerTradingRate,
          let totalTrades = trade.makerTotalTrades else { return }
    tradeView.setup(markerId: makerId , statusImage: nil, rate: tradeRate, totalTrades: totalTrades, distance: distance)
    
  }
  
  override func setupUI() {
    super.setupUI()
    
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
    let controller = P2PCreateOrderPopupViewController()
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

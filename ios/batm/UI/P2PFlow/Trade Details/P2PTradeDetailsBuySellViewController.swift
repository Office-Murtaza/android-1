import UIKit
import SnapKit
import MaterialComponents

class P2PTradeDetailsBuySellViewController: P2PTradeDetailsBaseViewController {
  private let tradeView =  P2PTradeDetailsRateView()
  private let tradeViewSeparator = P2PSeparatorView()
  
  private let infoMessageView = P2PTradeDetailsTextInfoView()
  
  private let buySellButton = MDCButton.buySell
  
  override func setupUI() {
    super.setupUI()
    
    stackView.addArrangedSubviews([
      tradeView,
      tradeViewSeparator,
      infoMessageView,
      buySellButton
    ])
    
    infoMessageView.update(message: "Selling cryptocurrency at the best rate. Ready to meet select cash as a method of payment. Always available, write in chat 24/7.")
    
  }
  
  override func setupLayout() {
    super.setupLayout()
    
    let separatorHeight = 1 / UIScreen.main.scale
    
    tradeView.snp.makeConstraints {
      $0.top.equalTo(paymentMethodsSeparator.snp.bottom)
      $0.left.right.equalToSuperview()
      $0.height.equalTo(56)
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
    
    buySellButton.snp.makeConstraints {
      $0.top.equalTo(infoMessageView.snp.bottom)
      $0.right.equalToSuperview().offset(-15)
      $0.left.equalToSuperview().offset(15)
      $0.height.equalTo(50)
    }
  }
  
}

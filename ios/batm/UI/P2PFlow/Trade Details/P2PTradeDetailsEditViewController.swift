import UIKit
import SnapKit
import MaterialComponents

class P2PTradeDetailsEditViewController: P2PTradeDetailsBaseViewController {
  
  private let openOrders = P2PTradeDetailsOpenOrdersView()
  private let openOrdersSeparator = P2PSeparatorView()
  
  private let infoMessageView = P2PTradeDetailsTextInfoView()
  private let editButton = MDCButton.edit
  private let cancelButton = MDCButton.cancelTransparent
  private let offsetView = UIView()
  private var balance: CoinsBalance?
  
  func setup(trade: Trade, balance: CoinsBalance) {
    super.setup(trade: trade)
    self.balance = balance
  }
  
  override func setupUI() {
    super.setupUI()
    stackView.addArrangedSubviews([
      openOrders,
      openOrdersSeparator,
      infoMessageView,
      editButton,
      offsetView,
      cancelButton,
      
    ])
    
    infoMessageView.update(message: "Selling cryptocurrency at the best rate. Ready to meet select cash as a method of payment. Always available, write in chat 24/7.")
    
    editButton.addTarget(self, action: #selector(editTrade), for: .touchUpInside)
    
  }
  
  @objc func editTrade() {
    
    guard let balance = balance, let trade = trade else { return }
    
    let tradeType = P2PSellBuyViewType(rawValue: trade.type ?? 2) ?? .buy
 
 
     let editTradeData = P2PEditTradeDataModel(id: trade.id ?? "",
                                               price: trade.price ?? 0,
                                               minLimit: Int(trade.minLimit ?? 0),
                                               maxLimit: Int(trade.maxLimit ?? 0),
                                               paymentMethods: trade.paymentMethods ?? "",
                                               terms: trade.terms ?? "")
 
     let controller = P2PEditTradeViewController(balance: balance,
                                                 payments: TradePaymentMethods.allCases,
                                                 delegate: self,
                                                 editModel: editTradeData,
                                                 coin: trade.coin ?? "",
                                                 tradeType: tradeType)
 
     navigationController?.pushViewController(controller, animated: true)
  }
  
  override func setupLayout() {
    super.setupLayout()
    
    let separatorHeight = 1 / UIScreen.main.scale
    
    openOrders.snp.makeConstraints {
      $0.top.equalTo(paymentMethodsSeparator.snp.bottom)
      $0.left.right.equalToSuperview()
      $0.height.equalTo(56)
    }
    
    openOrdersSeparator.snp.makeConstraints {
      $0.top.equalTo(openOrders.snp.bottom)
      $0.height.equalTo(separatorHeight)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
    }
    
    infoMessageView.snp.makeConstraints {
      $0.top.equalTo(openOrdersSeparator.snp.bottom)
      $0.left.right.equalToSuperview()
    }
    
    editButton.snp.makeConstraints {
      $0.top.equalTo(infoMessageView.snp.bottom)
      $0.right.equalToSuperview().offset(-15)
      $0.left.equalToSuperview().offset(15)
      $0.height.equalTo(50)
    }
    
    offsetView.snp.makeConstraints {
      $0.top.equalTo(editButton.snp.bottom)
      $0.right.left.equalToSuperview()
      $0.height.equalTo(5)
    }
    
    cancelButton.snp.makeConstraints {
      $0.top.equalTo(offsetView.snp.bottom)
      $0.right.equalToSuperview().offset(-15)
      $0.left.equalToSuperview().offset(15)
      $0.height.equalTo(50)
    }
  }
}

extension P2PTradeDetailsEditViewController: P2PEditTradeViewControllerDelegate {
  func didSelectEdit(data: P2PEditTradeDataModel) {
    print("selected edit", data)
  }
}

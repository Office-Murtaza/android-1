import UIKit

protocol P2PCreateTradeSellBuyViewDelegate: AnyObject {
    func didSelectedType(_ type: P2PSellBuyViewType)
}

class P2PCreateTradeSellBuyView: UIView {
    
    let buyView = P2PSellBuyView(radius: 16)
    let sellView = P2PSellBuyView(radius: 16)
    var delegate: P2PCreateTradeSellBuyViewDelegate?
  
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

  func setActive(type: P2PSellBuyViewType) {
    switch type {
    case .buy:
      sellView.setInactive()
      sellView.setTapEnabled(false)
      buyView.didSelected()
      buyView.setTapEnabled(false)
    case .sell:
      buyView.setInactive()
      buyView.setTapEnabled(false)
      sellView.didSelected()
      sellView.setTapEnabled(false)
    }
  }
  
    private func setupUI() {
        buyView.update(type: .buy)
        sellView.update(type: .sell)
        buyView.delegate = self
        sellView.delegate = self
        buyView.setSelected(true)
        
        addSubviews([
            buyView,
            sellView
        ])
    }
    
    private func setupLayout() {
        
        buyView.snp.makeConstraints {
            $0.left.equalToSuperview()
            $0.width.equalTo(70)
            $0.height.equalTo(32)
            $0.centerY.equalToSuperview()
        }
        
        sellView.snp.makeConstraints {
            $0.left.equalTo(buyView.snp.right).offset(10)
            $0.centerY.equalTo(buyView.snp.centerY)
            $0.width.equalTo(70)
            $0.height.equalTo(32)
        }
    }
}

extension P2PCreateTradeSellBuyView: P2PSellBuyViewDelegate {
  func didTap(view: P2PSellBuyView) {
    buyView.setSelected(false)
    sellView.setSelected(false)
    view.setSelected(true)
    delegate?.didSelectedType(view.currentType ?? .buy)
  }
}

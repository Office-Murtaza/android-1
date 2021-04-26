
import UIKit
import SnapKit

class P2PTradeDetailsRateView: UIView {
  private let markerIdView = MarkerIdView()
  private let rateView = P2PCellRateView()
  private let distanceView = P2PDistanceView()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
    setupLayout()
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    addSubviews([
      markerIdView,
      rateView,
      distanceView
    ])
  }
  
  private func setupLayout() {
    
    let separatorHeight = 1 / UIScreen.main.scale
    
    markerIdView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(16)
      $0.left.equalToSuperview().offset(16)
    }
    
    rateView.snp.makeConstraints {
      $0.top.equalTo(markerIdView.snp.bottom).offset(5)
      $0.left.equalTo(markerIdView.snp.left)
    }
    
    distanceView.snp.makeConstraints {
      $0.right.equalToSuperview().offset(-16)
      $0.left.equalTo(rateView.snp.right).offset(30)
      $0.centerY.equalToSuperview()
    }
  }
  
}

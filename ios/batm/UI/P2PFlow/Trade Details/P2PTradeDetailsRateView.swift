
import UIKit
import SnapKit

class P2PTradeDetailsRateView: UIView {
  private let markerIdView = MarkerIdView()
  private let rateView = P2PCellRateView()
  private let distanceView = P2PDistanceView()
  private let verticalSeparator = UIView()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
    setupLayout()
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func setup(markerId: String,
             statusImage: UIImage?,
             rate: Double,
             totalTrades: Double,
             distance: String) {
    
    markerIdView.update(markerId: markerId, statusImage: statusImage)
    rateView.update(rate: rate.formatted() ?? "0", tradesCount: totalTrades.formatted() ?? "0")
    distanceView.update(distance: distance, isDistanceNeeded: true)
  }
  
  private func setupUI() {
    addSubviews([
      markerIdView,
      rateView,
      distanceView,
      verticalSeparator
    ])
    
    verticalSeparator.backgroundColor = UIColor(hexString: "#212121", alpha: 0.8)
  }
  
  private func setupLayout() {
    
    let separatorWidth = 1 / UIScreen.main.scale
    
    markerIdView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(16)
      $0.left.equalToSuperview().offset(16)
    }
    
    rateView.snp.makeConstraints {
      $0.top.equalTo(markerIdView.snp.bottom).offset(5)
      $0.left.equalTo(markerIdView.snp.left)
    }
    
    verticalSeparator.snp.makeConstraints {
      $0.top.equalToSuperview().offset(16)
      $0.bottom.equalToSuperview().offset(-16)
      $0.width.equalTo(separatorWidth)
    }
    
    distanceView.snp.makeConstraints {
      $0.right.equalToSuperview().offset(-16)
      $0.left.equalTo(verticalSeparator.snp.right).offset(30)
      $0.centerY.equalToSuperview()
    }
  }
  
}

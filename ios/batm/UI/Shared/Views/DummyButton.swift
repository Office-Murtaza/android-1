import UIKit

class DummyButton: UIButton {
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
  }
  
  override var intrinsicContentSize: CGSize {
    return CGSize(width: UIView.noIntrinsicMetric, height: UIView.noIntrinsicMetric)
  }
  
}

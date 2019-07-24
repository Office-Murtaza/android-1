import UIKit

class PinCodeDotView: UIView {
  
  let size: CGFloat = 8
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    layer.cornerRadius = size / 2
  }
  
  private func setupLayout() {
    snp.makeConstraints {
      $0.size.equalTo(size)
    }
  }
}

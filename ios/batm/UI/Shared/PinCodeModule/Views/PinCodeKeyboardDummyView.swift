import UIKit
import RxSwift
import RxCocoa

class PinCodeKeyboardDummyView: UIView {
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    backgroundColor = .whiteTwo
  }
}

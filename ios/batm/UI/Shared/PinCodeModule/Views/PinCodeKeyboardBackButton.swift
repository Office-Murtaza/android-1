import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

class PinCodeKeyboardBackButton: MDCButton {
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    setBackgroundColor(.whiteTwo, for: .normal)
    setImageTintColor(.slateGrey, for: .normal)
    setImage(UIImage(named: "keyboard_back"), for: .normal)
    inkColor = .whiteFive
  }
}

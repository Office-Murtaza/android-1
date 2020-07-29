import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

class PinCodeKeyboardDigitButton: MDCButton {
  
  let digit: Int
  
  init(digit: Int) {
    self.digit = digit
    
    super.init(frame: .null)
    
    setupUI()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    setBackgroundColor(.whiteTwo, for: .normal)
    setTitleFont(.systemFont(ofSize: 20, weight: .bold), for: .normal)
    setTitleColor(.slateGrey, for: .normal)
    setTitle(String(digit), for: .normal)
    inkColor = .whiteFive
  }
}

extension Reactive where Base == PinCodeKeyboardDigitButton {
  var digitTapped: Driver<String> {
    let digit = String(base.digit)
    return base.rx.tap.asDriver().map { digit }
  }
}

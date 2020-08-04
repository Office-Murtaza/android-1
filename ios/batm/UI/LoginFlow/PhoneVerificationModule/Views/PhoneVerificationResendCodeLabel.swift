import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class PhoneVerificationResendCodeLabel: UILabel {
  
  static let resendCodeRange = NSRange(location:21, length: 11)
  
  let resendCodeTapRecognizer = UITapGestureRecognizer()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    let title = localize(L.PhoneVerification.resendCode)
    let attributes: [NSAttributedString.Key: Any] = [.font: UIFont.systemFont(ofSize: 15, weight: .medium)]
    let attributedString = NSMutableAttributedString(string: title, attributes: attributes)
    
    attributedString.addAttribute(.foregroundColor,
                                value: UIColor.slateGrey,
                                range: NSRange(location:0,length:21))
    
    attributedString.addAttributes([.foregroundColor: UIColor.ceruleanBlue],
                                 range: Self.resendCodeRange)
    
    attributedText = attributedString
    isUserInteractionEnabled = true
    addGestureRecognizer(resendCodeTapRecognizer)
  }
}

extension Reactive where Base == PhoneVerificationResendCodeLabel {
  var tap: Driver<Void> {
    return base.resendCodeTapRecognizer.rx.event
      .asDriver()
      .filter { [base] tapRecognizer in
        return tapRecognizer.didTapAttributedTextInLabel(label: base, inRange: PhoneVerificationResendCodeLabel.resendCodeRange)
    }
    .map { _ in () }
  }
}

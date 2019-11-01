import UIKit
import RxSwift
import RxCocoa

class TermsAndConditionsView: UIView, HasDisposeBag {
  
  let tapRecognizer = UITapGestureRecognizer()
  
  let checkboxView = CheckboxView()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    let title = localize(L.Welcome.termsAndConditions)
    let attributes: [NSAttributedString.Key: Any] = [.font: UIFont.poppinsRegular12]
    let attributedText = NSMutableAttributedString(string: title, attributes: attributes)
    attributedText.addAttribute(.foregroundColor,
                                value: UIColor.slateGrey,
                                range: NSRange(location:0,length:6))
    attributedText.addAttributes([.foregroundColor: UIColor.ceruleanBlue,
                                  .underlineStyle: NSUnderlineStyle.single.rawValue],
                                 range: NSRange(location:7,length: title.count - 7))
    label.attributedText = attributedText
    return label
  }()
  
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
    
    addSubviews(checkboxView, titleLabel)
    addGestureRecognizer(tapRecognizer)
  }
  
  private func setupLayout() {
    checkboxView.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
    }
    titleLabel.snp.makeConstraints {
      $0.left.equalTo(checkboxView.snp.right).offset(10)
      $0.right.centerY.equalToSuperview()
    }
  }
}

extension Reactive where Base == TermsAndConditionsView {
  var isAccepted: Driver<Bool> {
    return base.checkboxView.rx.isAccepted
  }
  
  var termsAndConditionsTap: Driver<Void> {
    return base.tapRecognizer.rx.event
      .asDriver()
      .filter { [base] tapRecognizer in
        let tappableTextRange = NSRange(location: 7, length: base.titleLabel.text!.count - 7)
        return tapRecognizer.didTapAttributedTextInLabel(label: base.titleLabel, inRange: tappableTextRange)
      }
      .map { _ in () }
  }
}

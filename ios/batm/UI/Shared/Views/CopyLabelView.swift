import UIKit
import RxSwift
import RxCocoa

class CopyLabelView: UIView {
  
  let tapRecognizer = UITapGestureRecognizer()
  
  let copyLabel: UILabel = {
    let label = UILabel()
    let attributes: [NSAttributedString.Key: Any] = [.font: UIFont.poppinsRegular12,
                                                     .foregroundColor: UIColor.ceruleanBlue,
                                                     .underlineStyle: NSUnderlineStyle.single.rawValue]
    let attributedText = NSAttributedString(string: localize(L.Shared.copy), attributes: attributes)
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
    
    addSubview(copyLabel)
    addGestureRecognizer(tapRecognizer)
  }
  
  private func setupLayout() {
    copyLabel.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
}

extension Reactive where Base == CopyLabelView {
  var tap: Driver<Void> {
    return base.tapRecognizer.rx.event
      .asDriver()
      .filter { [base] tapRecognizer in
        let tappableTextRange = NSRange(location: 0, length: base.copyLabel.text!.count)
        return tapRecognizer.didTapAttributedTextInLabel(label: base.copyLabel, inRange: tappableTextRange)
      }
      .map { _ in () }
  }
}

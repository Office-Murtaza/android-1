import UIKit
import RxSwift
import RxCocoa

enum UnderlinedLabelViewType {
  case copy
  case paste
  case scan
  case max
}

class UnderlinedLabelView: UIView {
  
  let underlinedLabel = UILabel()
  
  let dummyButton = DummyButton()
  
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
    
    addSubviews(underlinedLabel,
                dummyButton)
  }
  
  private func setupLayout() {
    underlinedLabel.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    dummyButton.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  func configure(for type: UnderlinedLabelViewType) {
    let attributes: [NSAttributedString.Key: Any] = [.font: UIFont.poppinsRegular12,
                                                     .foregroundColor: UIColor.ceruleanBlue,
                                                     .underlineStyle: NSUnderlineStyle.single.rawValue]
    let attributedText: NSAttributedString
    
    switch type {
    case .copy: attributedText = NSAttributedString(string: localize(L.Shared.copy), attributes: attributes)
    case .paste: attributedText = NSAttributedString(string: localize(L.Shared.paste), attributes: attributes)
    case .scan: attributedText = NSAttributedString(string: localize(L.Shared.scan), attributes: attributes)
    case .max: attributedText = NSAttributedString(string: localize(L.Shared.max), attributes: attributes)
    }
    
    underlinedLabel.attributedText = attributedText
  }
  
}

extension Reactive where Base == UnderlinedLabelView {
  var tap: Driver<Void> {
    return base.dummyButton.rx.tap.asDriver()
  }
}

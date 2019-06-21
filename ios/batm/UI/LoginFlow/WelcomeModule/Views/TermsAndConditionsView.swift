import UIKit
import RxSwift
import RxCocoa

class TermsAndConditionsView: UIView, HasDisposeBag {
  
  let tapRecognizer = UITapGestureRecognizer()
  
  let checkboxButton: UIButton = {
    let button = UIButton(type: .system)
    button.layer.borderColor = UIColor.greyish.withAlphaComponent(0.5).cgColor
    button.layer.borderWidth = 1
    button.layer.cornerRadius = 2
    return button
  }()
  
  let checkmarkImageView = UIImageView(image: UIImage(named: "welcome_checkmark"))
  
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
  
  var isAccepted: Bool {
    return checkmarkImageView.superview != nil
  }
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
    setupBindings()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(checkboxButton,
                titleLabel)
    addGestureRecognizer(tapRecognizer)
  }
  
  private func setupLayout() {
    checkboxButton.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
      $0.size.equalTo(15)
    }
    titleLabel.snp.makeConstraints {
      $0.left.equalTo(checkboxButton.snp.right).offset(10)
      $0.right.centerY.equalToSuperview()
    }
  }
  
  private func setupBindings() {
    checkboxButton.rx.tap
      .asDriver()
      .map { _ in () }
      .drive(onNext: { [unowned self] in self.toggleCheckbox() })
      .disposed(by: disposeBag)
  }
  
  private func toggleCheckbox() {
    if isAccepted {
      checkmarkImageView.removeFromSuperview()
    } else {
      checkboxButton.addSubview(checkmarkImageView)
      checkmarkImageView.snp.remakeConstraints {
        $0.left.bottom.equalToSuperview().inset(2)
      }
    }
  }
}

extension Reactive where Base == TermsAndConditionsView {
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

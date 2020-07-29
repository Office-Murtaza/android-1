import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class PhoneVerificationFormView: UIView, HasDisposeBag {
  
  let imageView = UIImageView(image: UIImage(named: "login_sms_code"))
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.PhoneVerification.enterCode)
    label.textColor = .warmGrey
    label.font = .systemFont(ofSize: 16)
    label.textAlignment = .center
    label.numberOfLines = 2
    return label
  }()
  
  let codeTextField: CodeInputView = {
    let view = CodeInputView()
    view.isUserInteractionEnabled = true
    return view
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
    
    addSubviews(imageView,
                titleLabel,
                codeTextField)
  }
  
  private func setupLayout() {
    imageView.snp.makeConstraints {
      $0.top.equalToSuperview()
      $0.centerX.equalToSuperview()
      $0.keepRatio(for: imageView)
    }
    imageView.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
    imageView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
    
    titleLabel.snp.makeConstraints {
      $0.top.equalTo(imageView.snp.bottom).offset(50)
      $0.left.right.equalToSuperview()
    }
    
    codeTextField.snp.makeConstraints {
      $0.top.equalTo(titleLabel.snp.bottom).offset(30)
      $0.centerX.bottom.equalToSuperview()
    }
  }
}

extension Reactive where Base == PhoneVerificationFormView {
  var codeText: Driver<String> {
    return base.codeTextField.rx.code.asDriver()
  }
}

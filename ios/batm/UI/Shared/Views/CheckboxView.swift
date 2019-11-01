import UIKit
import RxSwift
import RxCocoa

class CheckboxView: UIView, HasDisposeBag {
  
  let tapRecognizer = UITapGestureRecognizer()
  
  let checkboxButton: UIButton = {
    let button = UIButton(type: .system)
    button.layer.borderColor = UIColor.greyish.withAlphaComponent(0.5).cgColor
    button.layer.borderWidth = 1
    button.layer.cornerRadius = 2
    return button
  }()
  
  let checkmarkImageView = UIImageView(image: UIImage(named: "welcome_checkmark"))
  
  let isAcceptedRelay = BehaviorRelay<Bool>(value: false)
  
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
                checkmarkImageView)
    addGestureRecognizer(tapRecognizer)
  }
  
  private func setupLayout() {
    checkboxButton.snp.makeConstraints {
      $0.edges.equalToSuperview()
      $0.size.equalTo(15)
    }
    checkmarkImageView.snp.remakeConstraints {
      $0.left.bottom.equalToSuperview().inset(2)
    }
  }
  
  private func setupBindings() {
    checkboxButton.rx.tap
      .subscribe(onNext: { [isAcceptedRelay] in isAcceptedRelay.toggle() })
      .disposed(by: disposeBag)
    
    isAcceptedRelay.not()
      .bind(to: checkmarkImageView.rx.isHidden)
      .disposed(by: disposeBag)
  }
}

extension Reactive where Base == CheckboxView {
  var isAccepted: Driver<Bool> {
    return base.isAcceptedRelay.asDriver()
  }
}

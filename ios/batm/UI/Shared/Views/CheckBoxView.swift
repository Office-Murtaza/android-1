import UIKit
import RxSwift
import RxCocoa

class CheckBoxView: UIView, HasDisposeBag {
  
  let isAcceptedRelay = BehaviorRelay<Bool>(value: false)
  
  let tapRecognizer = UITapGestureRecognizer()
  
  let checkboxButton: UIButton = {
    let button = UIButton(type: .system)
    button.layer.borderWidth = 1
    button.layer.cornerRadius = 3
    return button
  }()
  
  let checkmarkImageView = UIImageView(image: UIImage(named: "white_tick"))
  
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
      $0.size.equalTo(22)
    }
    checkmarkImageView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
  }
  
  private func setupBindings() {
    checkboxButton.rx.tap
      .subscribe(onNext: { [isAcceptedRelay] in isAcceptedRelay.toggle() })
      .disposed(by: disposeBag)
    
    isAcceptedRelay
      .subscribe(onNext: { [unowned self] isAccepted in
        self.checkmarkImageView.isHidden = !isAccepted
        self.checkboxButton.layer.borderColor = isAccepted ? UIColor.ceruleanBlue.cgColor : UIColor.pinkishGrey.cgColor
        self.checkboxButton.backgroundColor = isAccepted ? .ceruleanBlue : .clear
      })
      .disposed(by: disposeBag)
  }
  
  func set(accepted: Bool) {
    isAcceptedRelay.accept(accepted)
  }
}

extension Reactive where Base == CheckBoxView {
  var isAccepted: Driver<Bool> {
    return base.isAcceptedRelay.asDriver()
  }
}
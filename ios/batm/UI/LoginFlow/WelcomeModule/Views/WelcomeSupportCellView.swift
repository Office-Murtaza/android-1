import UIKit
import RxSwift
import RxCocoa

enum WelcomeSupportCellViewType {
  case phone
  case mail
}

class WelcomeSupportCellView: UIView {
  
  let roundedView: UIView = {
    let view = UIView()
    view.backgroundColor = .lightGold
    view.layer.cornerRadius = 16
    return view
  }()
  
  let imageView = UIImageView(image: nil)
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .poppinsMedium14
    return label
  }()
  
  let copyLabel = CopyLabelView()
  
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
    
    addSubviews(roundedView,
                titleLabel,
                copyLabel)
    
    roundedView.addSubview(imageView)
  }
  
  private func setupLayout() {
    roundedView.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
      $0.size.equalTo(46)
    }
    imageView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    titleLabel.snp.makeConstraints {
      $0.top.equalToSuperview().offset(4)
      $0.left.equalTo(roundedView.snp.right).offset(16)
    }
    copyLabel.snp.makeConstraints {
      $0.bottom.equalToSuperview().offset(-4)
      $0.left.equalTo(roundedView.snp.right).offset(16)
    }
  }
  
  func configure(for type: WelcomeSupportCellViewType) {
    switch type {
    case .phone:
      titleLabel.text = localize(L.Welcome.Support.phone)
      imageView.image = UIImage(named: "welcome_phone")
    case .mail:
      titleLabel.text = localize(L.Welcome.Support.mail)
      imageView.image = UIImage(named: "welcome_mail")
    }
  }
}

extension Reactive where Base == WelcomeSupportCellView {
  var copyTap: Driver<Void> {
    return base.copyLabel.rx.tap
  }
}

import UIKit
import RxSwift
import RxCocoa

enum WelcomeSlideViewType {
  case first
  case second
  case third
}

class WelcomeSlideView: UIView {
  
  let imageView = UIImageView(image: nil)
  let titleLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .systemFont(ofSize: 20, weight: .bold)
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
    
    addSubviews(imageView,
                titleLabel)
  }
  
  private func setupLayout() {
    imageView.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
    imageView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
    titleLabel.snp.makeConstraints {
      $0.top.equalTo(imageView.snp.bottom).offset(30)
      $0.bottom.centerX.equalToSuperview()
    }
  }
  
  func configure(for type: WelcomeSlideViewType) {
    switch type {
    case .first:
      imageView.image = UIImage(named: "welcome_slide_1")
      titleLabel.text = localize(L.Welcome.FirstSlide.title)
    case .second:
      imageView.image = UIImage(named: "welcome_slide_2")
      titleLabel.text = localize(L.Welcome.SecondSlide.title)
    case .third:
      imageView.image = UIImage(named: "welcome_slide_3")
      titleLabel.text = localize(L.Welcome.ThirdSlide.title)
    }
    
    imageView.snp.makeConstraints {
      $0.top.centerX.equalToSuperview()
      $0.keepRatio(for: imageView)
    }
  }
}

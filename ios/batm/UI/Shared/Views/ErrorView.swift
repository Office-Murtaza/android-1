import UIKit
import RxSwift
import RxCocoa

class ErrorView: UIView {
  
  let imageView = UIImageView(image: UIImage(named: "error"))
  
  let label: UILabel = {
    let label = UILabel()
    label.textColor = .orangeyRed
    label.font = .poppinsMedium10
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.7
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
                label)
  }
  
  private func setupLayout() {
    imageView.snp.makeConstraints {
      $0.left.centerY.equalToSuperview()
    }
    label.snp.makeConstraints {
      $0.left.equalTo(imageView.snp.right).offset(7)
      $0.top.right.bottom.equalToSuperview()
    }
  }
  
  func configure(for message: String?) {
    label.text = message
  }
}

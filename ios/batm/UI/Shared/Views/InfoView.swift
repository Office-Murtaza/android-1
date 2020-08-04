import UIKit
import RxSwift
import RxCocoa

class InfoView: UIView {
  
  let imageView = UIImageView(image: UIImage(named: "info"))
  
  let label: UILabel = {
    let label = UILabel()
    label.numberOfLines = 0
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
    
    layer.cornerRadius = 4
    backgroundColor = .duckEggBlue
    
    addSubviews(imageView, label)
  }
  
  private func setupLayout() {
    imageView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(20)
      $0.centerX.equalToSuperview()
    }
    label.snp.makeConstraints {
      $0.top.equalTo(imageView.snp.bottom).offset(15)
      $0.left.right.equalToSuperview().inset(35)
      $0.bottom.equalToSuperview().offset(-15)
    }
  }
  
  func setup(with text: String) {
    let paragraphStyle = NSMutableParagraphStyle()
    paragraphStyle.lineSpacing = 10
    paragraphStyle.alignment = .center
    
    let attributedString = NSAttributedString(string: text, attributes: [.foregroundColor: UIColor.ceruleanBlue,
                                                                         .font: UIFont.systemFont(ofSize: 16),
                                                                         .paragraphStyle: paragraphStyle])
    
    label.attributedText = attributedString
  }
}

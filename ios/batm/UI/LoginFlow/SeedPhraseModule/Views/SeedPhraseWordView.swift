import UIKit
import RxSwift
import RxCocoa

class SeedPhraseWordView: UIView {
  
  let titleLabel = UILabel()
  
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
    
    layer.cornerRadius = 3
    layer.borderColor = UIColor.whiteFive.cgColor
    layer.borderWidth = 1
    
    addSubview(titleLabel)
  }
  
  private func setupLayout() {
    titleLabel.snp.makeConstraints {
      $0.edges.equalToSuperview().inset(8)
    }
  }
  
  func configure(for word: String, by index: Int) {
    let title = "\(index) \(word)"
    let attributes: [NSAttributedString.Key: Any] = [
      .font: UIFont.poppinsMedium11,
      .foregroundColor: UIColor.pinkishGreyTwo
    ]
    let attributedText = NSMutableAttributedString(string: title, attributes: attributes)
    
    attributedText.addAttribute(.foregroundColor,
                                  value: UIColor.slateGrey,
                                  range: NSRange(location: title.count - word.count, length: word.count))
    
    titleLabel.attributedText = attributedText
  }
}

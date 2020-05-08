import UIKit
import RxSwift
import RxCocoa

class CoinSellDetailsInstructionView: UIView {
  
  let numberLabel: UILabel = {
    let label = UILabel()
    label.textColor = .ceruleanBlue
    label.font = .systemFont(ofSize: 16, weight: .medium)
    return label
  }()
  
  let instructionLabel: UILabel = {
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
    
    addSubviews(numberLabel,
                instructionLabel)
  }
  
  private func setupLayout() {
    numberLabel.snp.makeConstraints {
      $0.top.left.equalToSuperview()
    }
    numberLabel.setContentCompressionResistancePriority(.required, for: .horizontal)
    numberLabel.setContentHuggingPriority(.required, for: .horizontal)
    instructionLabel.snp.makeConstraints {
      $0.top.right.bottom.equalToSuperview()
      $0.left.equalTo(numberLabel.snp.right).offset(10)
    }
  }
  
  func configure(number: Int, text: String) {
    numberLabel.text = "\(number)."
    
    let paragraphStyle = NSMutableParagraphStyle()
    paragraphStyle.lineSpacing = 10
    
    let attributedString = NSAttributedString(string: text, attributes: [.foregroundColor: UIColor.warmGrey,
                                                                         .font: UIFont.systemFont(ofSize: 16),
                                                                         .paragraphStyle: paragraphStyle])
    
    instructionLabel.attributedText = attributedString
  }
}

import UIKit
import RxSwift
import RxCocoa

class CoinSellDetailsInstructionView: UIView {
  
  let numberLabel: UILabel = {
    let label = UILabel()
    label.textColor = .ceruleanBlue
    label.font = .poppinsBold13
    return label
  }()
  
  let instructionLabel: UILabel = {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = .poppinsMedium13
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
    instructionLabel.text = text
  }
}

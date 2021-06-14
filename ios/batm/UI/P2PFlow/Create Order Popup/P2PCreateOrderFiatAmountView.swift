import UIKit
import SnapKit

class P2PCreateOrderFiatAmountView: UIView {
  
  lazy var fiatAmountTitle: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    label.textColor = UIColor.black.withAlphaComponent(0.6)
    return label
  }()
  
  var fiatTextField: UITextField = {
    let textField = UITextField()
    textField.font = .systemFont(ofSize: 22)
    textField.keyboardType = .decimalPad
    textField.adjustsFontSizeToFitWidth = true
    textField.minimumFontSize = 10
    textField.textAlignment = .right
    return textField
  }()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
    setupLayout()
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    
    addSubviews([
      fiatAmountTitle,
      fiatTextField
    ])
   
    fiatAmountTitle.text = localize(L.P2p.Fiat.Amount.title)
    fiatTextField.text = "0"
  }
  
  private func setupLayout() {
    fiatAmountTitle.snp.makeConstraints {
      $0.top.equalToSuperview().offset(24)
      $0.right.equalToSuperview()
    }
   
    fiatTextField.snp.makeConstraints {
      $0.top.equalTo(fiatAmountTitle.snp.bottom).offset(8)
      $0.right.equalToSuperview()
      $0.left.equalToSuperview()
      $0.bottom.equalToSuperview()
    }
    
  }
  
}

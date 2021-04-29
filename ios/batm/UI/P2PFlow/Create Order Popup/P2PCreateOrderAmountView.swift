import UIKit
import SnapKit

class P2PCreateOrderAmountView: UIView {
  
  lazy var cryptoAmountTitle: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    label.textColor = UIColor.black.withAlphaComponent(0.6)
    return label
  }()
  
  
  lazy var cryptoAmountValue: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 22)
    label.textColor = UIColor.black
    return label
  }()
  
  lazy var fiatAmountTitle: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    label.textColor = UIColor.black.withAlphaComponent(0.6)
    return label
  }()
  
  var fiatTextField: UITextField = {
    let textField = UITextField()
    textField.font = .systemFont(ofSize: 22)
    textField.keyboardType = .phonePad
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
    backgroundColor = UIColor(hexString: "#8D8D8D", alpha: 0.1)
    
    addSubviews([
      cryptoAmountTitle,
      cryptoAmountValue,
      fiatAmountTitle,
      fiatTextField
    ])
    
    cryptoAmountTitle.text = "Crypto amount"
    fiatAmountTitle.text = "Fiat amount"
    
    cryptoAmountValue.text = "0"
    fiatTextField.text = "0"
  }
  
  private func setupLayout() {
    cryptoAmountTitle.snp.makeConstraints {
      $0.top.equalToSuperview().offset(24)
      $0.left.equalToSuperview().offset(15)
    }
    
    cryptoAmountValue.snp.makeConstraints {
      $0.top.equalTo(cryptoAmountTitle.snp.bottom).offset(8)
      $0.left.equalTo(cryptoAmountTitle.snp.left)
    }
    
    fiatAmountTitle.snp.makeConstraints {
      $0.right.equalToSuperview().offset(-15)
      $0.top.equalTo(cryptoAmountTitle.snp.top)
    }
   
    fiatTextField.snp.makeConstraints {
      $0.top.equalTo(fiatAmountTitle.snp.bottom)
      $0.right.equalToSuperview().offset(-15)
    }
    
  }
  
}

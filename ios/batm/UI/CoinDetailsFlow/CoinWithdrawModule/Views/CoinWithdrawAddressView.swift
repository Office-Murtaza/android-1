import UIKit
import RxSwift
import RxCocoa

class CoinWithdrawAddressView: UIView {
  
  let addressLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinWithdraw.Form.Address.title)
    label.textColor = .slateGrey
    label.font = .poppinsSemibold14
    return label
  }()

  let addressTextField: MainTextField = {
    let textField = MainTextField()
    textField.textAlignment = .center
    return textField
  }()

  let addressActionsContainer = UIView()

  let pasteActionContainer = UIView()

  let scanActionContainer = UIView()

  let pasteLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .paste)
    return label
  }()

  let scanLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .scan)
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
    
    addSubviews(addressLabel,
                addressTextField,
                addressActionsContainer)
    
    addressActionsContainer.addSubviews(pasteActionContainer,
                                        scanActionContainer)
    
    pasteActionContainer.addSubview(pasteLabel)
    scanActionContainer.addSubview(scanLabel)
  }
  
  private func setupLayout() {
    addressLabel.snp.makeConstraints {
      $0.top.centerX.equalToSuperview()
    }
    addressTextField.snp.makeConstraints {
      $0.top.equalTo(addressLabel.snp.bottom).offset(15)
      $0.left.right.equalToSuperview()
    }
    addressActionsContainer.snp.makeConstraints {
      $0.top.equalTo(addressTextField.snp.bottom)
      $0.left.right.equalTo(addressTextField)
      $0.bottom.equalToSuperview()
    }
    pasteActionContainer.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
    }
    scanActionContainer.snp.makeConstraints {
      $0.top.right.bottom.equalToSuperview()
      $0.left.equalTo(pasteActionContainer.snp.right)
      $0.width.equalTo(pasteActionContainer)
    }
    [pasteLabel, scanLabel].forEach {
      $0.snp.makeConstraints {
        $0.top.bottom.equalToSuperview().inset(15)
        $0.centerX.equalToSuperview()
      }
    }
  }
}

extension Reactive where Base == CoinWithdrawAddressView {
  var text: ControlProperty<String?> {
    return base.addressTextField.rx.text
  }
  
  var pasteTap: Driver<Void> {
    return base.pasteLabel.rx.tap
  }
  
  var scanTap: Driver<Void> {
    return base.scanLabel.rx.tap
  }
}

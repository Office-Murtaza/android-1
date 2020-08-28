import UIKit
import RxSwift
import RxCocoa
import MaterialComponents
import GiphyUISDK
import GiphyCoreSDK
import FlagPhoneNumber

final class CoinSendGiftFormView: UIView, HasDisposeBag {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let bottomContainer = UIView()
  
  let gifViewContainer: UIView = {
    let view = UIView()
    view.backgroundColor = .duckEggBlue
    view.layer.cornerRadius = 4
    view.layer.masksToBounds = true
    return view
  }()
  
  let gifMediaView: GPHMediaView = {
    let view = GPHMediaView()
    view.isHidden = true
    return view
  }()
  
  let addButton = MDCButton.plus
  let removeButton = MDCButton.close
  
  let phoneTextField = MDCTextField.phone
  let coinAmountTextFieldView = CoinAmountTextFieldView()
  let messageTextField = MDCMultilineTextField.default
  
  let phoneTextFieldController: MDCTextInputControllerOutlined
  let messageTextFieldController: MDCTextInputControllerOutlinedTextArea
  
  override init(frame: CGRect) {
    phoneTextFieldController = ThemedTextInputControllerOutlined(textInput: phoneTextField)
    messageTextFieldController = ThemedTextInputControllerOutlinedTextArea(textInput: messageTextField)
    
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(stackView,
                bottomContainer)
    stackView.addArrangedSubviews(phoneTextField,
                                  coinAmountTextFieldView)

    bottomContainer.addSubviews(gifViewContainer,
                                messageTextField)

    gifViewContainer.addSubviews(gifMediaView,
                                 removeButton,
                                 addButton)
    
    phoneTextFieldController.placeholderText = localize(L.CoinSendGift.Form.Phone.placeholder)
    messageTextFieldController.placeholderText = localize(L.CoinSendGift.Form.Message.placeholder)

    messageTextFieldController.minimumLines = 3
    
    removeButton.isHidden = true
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    bottomContainer.snp.makeConstraints {
      $0.top.equalTo(stackView.snp.bottom).offset(15)
      $0.left.right.bottom.equalToSuperview()
    }
    gifViewContainer.snp.makeConstraints {
      $0.top.left.equalToSuperview()
      $0.height.equalTo(messageTextField)
      $0.width.equalTo(gifViewContainer.snp.height)
    }
    gifMediaView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    messageTextField.snp.makeConstraints {
      $0.top.right.bottom.equalToSuperview()
      $0.left.equalTo(gifViewContainer.snp.right).offset(15)
      $0.height.equalTo(105)
    }
    [removeButton, addButton].forEach {
      $0.snp.makeConstraints {
        $0.size.equalTo(35)
        $0.bottom.right.equalToSuperview().inset(10)
      }
    }
  }
  
  func configure(with coinCode: String) {
    coinAmountTextFieldView.configure(with: coinCode)
  }
}

extension Reactive where Base == CoinSendGiftFormView {
  var phoneText: ControlProperty<String?> {
    return base.phoneTextField.rx.text
  }
  var fiatAmountText: Binder<String?> {
    return base.coinAmountTextFieldView.rx.fiatAmountText
  }
  var coinAmountText: ControlProperty<String?> {
    return base.coinAmountTextFieldView.rx.coinAmountText
  }
  var messageText: ControlProperty<String?> {
    return base.messageTextField.rx.text
  }
  var phoneErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.phoneTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var coinAmountErrorText: Binder<String?> {
    return base.coinAmountTextFieldView.rx.coinAmountErrorText
  }
  var messageErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.messageTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var maxTap: Driver<Void> {
    return base.coinAmountTextFieldView.rx.maxTap
  }
  var addGifTap: Driver<Void> {
    return base.addButton.rx.tap.asDriver()
  }
  var removeGifTap: Driver<Void> {
    return base.removeButton.rx.tap.asDriver()
  }
  var gifMedia: Binder<GPHMedia?> {
    return Binder(base) { target, value in
      target.removeButton.isHidden = value == nil
      target.addButton.isHidden = value != nil
      
      if let media = value {
        target.gifMediaView.setMedia(media, rendition: .fixedHeightSmall)
      } else {
        target.gifMediaView.media = nil
      }
      target.gifMediaView.isHidden = value == nil
    }
  }
}

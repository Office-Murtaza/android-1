import UIKit
import RxSwift
import RxCocoa
import MaterialComponents
import GiphyUISDK
import GiphyCoreSDK
import FlagPhoneNumber

final class CoinSendGiftFormView: UIView, HasDisposeBag {
  
  let didSelectCountry = PublishRelay<FPNCountry>()
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let bottomContainer = UIView()
  
  let gifViewContainer: UIView = {
    let view = UIView()
    view.backgroundColor = .whiteTwo
    view.layer.cornerRadius = 4
    view.layer.masksToBounds = true
    return view
  }()
  
  let gifEmptyImageView = UIImageView(image: UIImage(named: "send_gift"))
  
  let gifMediaView: GPHMediaView = {
    let view = GPHMediaView()
    view.isHidden = true
    return view
  }()
  
  let pasteButton = MDCButton.paste
  let coinMaxButton = MDCButton.max
  let currencyMaxButton = MDCButton.max
  let addButton = MDCButton.add
  let removeButton = MDCButton.remove
  
  let countryPicker = FPNCountryPicker()
  let countryRepository = FPNCountryRepository()
  
  let phoneFieldsContainer = UIView()
  let dialCodeContainer = UIView()
  let fakeDialCodeTextField = FakeTextField()
  
  let dialCodeTextField = MDCTextField.dialCode
  let phoneTextField = MDCTextField.phone
  let coinAmountTextField = MDCTextField.amount
  let currencyAmountTextField = MDCTextField.amount
  let messageTextField = MDCMultilineTextField.default
  
  let dialCodeTextFieldController: MDCTextInputControllerOutlined
  let phoneTextFieldController: MDCTextInputControllerOutlined
  let coinAmountTextFieldController: MDCTextInputControllerOutlined
  let currencyAmountTextFieldController: MDCTextInputControllerOutlined
  let messageTextFieldController: MDCTextInputControllerOutlinedTextArea
  
  override init(frame: CGRect) {
    dialCodeTextFieldController = MDCTextInputControllerOutlined(textInput: dialCodeTextField)
    phoneTextFieldController = MDCTextInputControllerOutlined(textInput: phoneTextField)
    coinAmountTextFieldController = MDCTextInputControllerOutlined(textInput: coinAmountTextField)
    currencyAmountTextFieldController = MDCTextInputControllerOutlined(textInput: currencyAmountTextField)
    messageTextFieldController = MDCTextInputControllerOutlinedTextArea(textInput: messageTextField)
    
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
    setupBindings()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubview(stackView)
    stackView.addArrangedSubviews(phoneFieldsContainer,
                                  coinAmountTextField,
                                  currencyAmountTextField,
                                  bottomContainer)

    phoneFieldsContainer.addSubviews(dialCodeContainer,
                                     phoneTextField)
    
    dialCodeContainer.addSubviews(dialCodeTextField,
                                  fakeDialCodeTextField)

    bottomContainer.addSubviews(gifViewContainer,
                                messageTextField,
                                addButton,
                                removeButton)

    gifViewContainer.addSubviews(gifEmptyImageView,
                                 gifMediaView)
    
    phoneTextField.setRightView(pasteButton)
    coinAmountTextField.setRightView(coinMaxButton)
    currencyAmountTextField.setRightView(currencyMaxButton)
    
    let scheme = MDCContainerScheme.default
    dialCodeTextFieldController.applyTheme(withScheme: scheme)
    phoneTextFieldController.applyTheme(withScheme: scheme)
    coinAmountTextFieldController.applyTheme(withScheme: scheme)
    currencyAmountTextFieldController.applyTheme(withScheme: scheme)
    
    dialCodeTextFieldController.placeholderText = localize(L.CoinSendGift.Form.Code.placeholder)
    phoneTextFieldController.placeholderText = localize(L.CoinSendGift.Form.Phone.placeholder)
    coinAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CoinAmount.placeholder)
    currencyAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CurrencyAmount.placeholder)
    messageTextFieldController.placeholderText = localize(L.CoinSendGift.Form.Message.placeholder)

    messageTextFieldController.minimumLines = 4
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    dialCodeContainer.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
      $0.width.equalTo(100)
    }
    dialCodeTextField.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    fakeDialCodeTextField.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    phoneTextField.snp.makeConstraints {
      $0.top.right.bottom.equalToSuperview()
      $0.left.equalTo(dialCodeContainer.snp.right).offset(15)
    }
    gifViewContainer.snp.makeConstraints {
      $0.top.left.equalToSuperview()
      $0.height.equalTo(messageTextField)
      $0.width.equalTo(gifViewContainer.snp.height)
    }
    gifEmptyImageView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    gifMediaView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    messageTextField.snp.makeConstraints {
      $0.top.right.equalToSuperview()
      $0.left.equalTo(gifViewContainer.snp.right).offset(15)
      $0.height.equalTo(107)
    }
    addButton.snp.makeConstraints {
      $0.top.equalTo(gifViewContainer.snp.bottom).offset(10)
      $0.left.equalTo(gifViewContainer)
      $0.bottom.equalToSuperview()
    }
    removeButton.snp.makeConstraints {
      $0.top.equalTo(addButton)
      $0.right.equalTo(gifViewContainer)
    }
  }
  
  private func setupBindings() {
    didSelectCountry
      .map { $0.phoneCode }
      .bind(to: dialCodeTextField.rx.text)
      .disposed(by: disposeBag)
    
    setupCountryPicker()
  }
  
  private func setupCountryPicker() {
    fakeDialCodeTextField.inputView = countryPicker
    
    countryPicker.setup(repository: countryRepository)
    
    countryPicker.didSelect = { [unowned self] country in
      self.didSelectCountry.accept(country)
    }

    if let countryCode = FPNCountryCode(rawValue: "US") {
      countryPicker.setCountry(countryCode)
    }
  }
  
  func configure(with coinCode: String) {
    coinAmountTextFieldController.placeholderText = "\(coinCode) \(localize(L.CoinWithdraw.Form.CoinAmount.placeholder))"
  }
}

extension Reactive where Base == CoinSendGiftFormView {
  var country: Driver<FPNCountry> {
    return base.didSelectCountry.asDriver(onErrorDriveWith: .empty())
  }
  var phoneText: ControlProperty<String?> {
    return base.phoneTextField.rx.text
  }
  var currencyText: ControlProperty<String?> {
    return base.currencyAmountTextField.rx.text
  }
  var coinText: ControlProperty<String?> {
    return base.coinAmountTextField.rx.text
  }
  var messageText: ControlProperty<String?> {
    return base.messageTextField.rx.text
  }
  var maxTap: Driver<Void> {
    return Driver.merge(base.coinMaxButton.rx.tap.asDriver(),
                        base.currencyMaxButton.rx.tap.asDriver())
  }
  var pasteTap: Driver<Void> {
    return base.pasteButton.rx.tap.asDriver()
  }
  var addGifTap: Driver<Void> {
    return base.addButton.rx.tap.asDriver()
  }
  var removeGifTap: Driver<Void> {
    return base.removeButton.rx.tap.asDriver()
  }
  var gifMedia: Binder<GPHMedia?> {
    return Binder(base) { target, value in
      if let media = value {
        target.gifMediaView.setMedia(media, rendition: .fixedHeightSmall)
      } else {
        target.gifMediaView.media = nil
      }
      target.gifMediaView.isHidden = value == nil
    }
  }
}

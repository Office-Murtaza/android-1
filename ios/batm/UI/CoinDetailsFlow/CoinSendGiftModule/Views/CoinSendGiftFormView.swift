import UIKit
import RxSwift
import RxCocoa
import MaterialComponents
import GiphyUISDK
import GiphyCoreSDK

final class CoinSendGiftFormView: UIView, HasDisposeBag {
    let bottomContainer = UIView()
    
    let gifViewContainer: UIView = {
        let view = UIView()
        view.layer.cornerRadius = 4
        view.layer.masksToBounds = true
        return view
    }()
    
    let gifMediaView: GPHMediaView = {
        let view = GPHMediaView()
        view.isHidden = true
        return view
    }()
    
    lazy var separatorView: UIView = {
        let separator = UIView()
        separator.backgroundColor = .lightGray
        return separator
    }()
    
    lazy var verticalSeparatorView: UIView = {
        let separator = UIView()
        separator.backgroundColor = .lightGray
        return separator
    }()
    
    lazy var addButton: MDCButton = {
        let button = MDCButton()
        button.setTitle(localize(L.CoinSendGift.addGif), for: .normal)
        button.backgroundColor = .clear
        button.setTitleFont(.systemFont(ofSize: 16, weight: .semibold), for: .normal)
        button.setTitleColor(UIColor(hexString: "#0073E4"), for: .normal)
        
        return button
    }()
    
    lazy var addMessageButton: MDCButton = {
        let button = MDCButton()
        button.setTitle(localize(L.CoinSendGift.addNote) , for: .normal)
        button.backgroundColor = .clear
        button.setTitleFont(.systemFont(ofSize: 16, weight: .semibold), for: .normal)
        button.setTitleColor(UIColor(hexString: "#0073E4"), for: .normal)
        button.addTarget(self, action: #selector(focusOnMessage) , for: .touchUpInside)
        
        return button
    }()
    
    lazy var removeButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage(named: "close"), for: .normal)
        button.backgroundColor = .white
        button.layer.cornerRadius = 12
        button.layer.shadowColor = UIColor.black.cgColor
        button.layer.shadowOffset = .zero
        button.layer.shadowRadius = 3
        button.layer.shadowOpacity = 0.5
        button.layer.shadowOffset = CGSize(width: 1, height: 2)
        
        return button
    }()
    
    lazy var messageTextField: MDCMultilineTextField = {
        let field = MDCMultilineTextField.default
        field.borderView = nil
        
        return field
    }()
    
    var coinAmountTextFieldView = CoinExchangeSwapTextFieldView()
    var messageTextFieldController: ThemedTextInputControllerOutlinedTextArea?
    private let usdView = SwapUsdValueView()

    override init(frame: CGRect) {
        super.init(frame: frame)
        messageTextFieldController = ThemedTextInputControllerOutlinedTextArea(textInput: messageTextField)
        setupUI()
        setupLayout()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(coin: CustomCoinType, fromCoins: [CustomCoinType]) {
        coinAmountTextFieldView.configure(for: coin, coins: fromCoins)
    }
    
    func configureFromError(error: String?) {
        configureField(field: &coinAmountTextFieldView, error: error)
    }
    
    func configureUsdView(usd: Decimal) {
        usdView.configure(value: usd)
    }
    
    @objc func focusOnMessage() {
        addMessageButton.isHidden = true
        messageTextField.becomeFirstResponder()
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        
        addSubviews([coinAmountTextFieldView, bottomContainer, separatorView, usdView])
        
        bottomContainer.addSubviews(gifViewContainer,
                                    removeButton,
                                    verticalSeparatorView,
                                    messageTextField,
                                    addMessageButton)
        
        gifViewContainer.addSubviews(gifMediaView,
                                     addButton)
        
        messageTextFieldController?.minimumLines = 3
        
        removeButton.isHidden = true
    }
    
    private func setupLayout() {
        separatorView.snp.makeConstraints{
            $0.top.equalTo(coinAmountTextFieldView.snp.bottom).offset(10)
            $0.left.equalToSuperview()
            $0.right.equalToSuperview()
            $0.height.equalTo(1/UIScreen.main.scale)
        }
        
        coinAmountTextFieldView.snp.remakeConstraints{
            $0.left.right.top.equalToSuperview()
            $0.height.equalTo(120)
        }
        
        usdView.snp.makeConstraints {
            $0.right.equalToSuperview()
            $0.top.equalTo(separatorView.snp.bottom).offset(10)
            $0.left.equalToSuperview()
            $0.height.equalTo(45)
        }
        
        bottomContainer.snp.makeConstraints {
            $0.top.equalTo(usdView.snp.bottom).offset(15)
            $0.left.right.bottom.equalToSuperview()
        }
        
        gifViewContainer.snp.makeConstraints {
            $0.top.left.equalToSuperview()
            $0.height.equalTo(messageTextField)
            $0.right.equalTo(bottomContainer.snp.centerX).offset(-15)
        }
        
        gifMediaView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
        
        verticalSeparatorView.snp.makeConstraints {
            $0.width.equalTo(1/UIScreen.main.scale)
            $0.centerX.equalTo(bottomContainer.snp.centerX)
            $0.top.bottom.equalToSuperview()
        }
        
        messageTextField.snp.makeConstraints {
            $0.top.right.bottom.equalToSuperview()
            $0.left.equalTo(bottomContainer.snp.centerX).offset(15)
            $0.height.equalTo(105)
        }
        
        addButton.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
        
        removeButton.snp.makeConstraints {
            $0.width.height.equalTo(24)
            $0.centerX.equalTo(gifViewContainer.snp.right)
            $0.centerY.equalTo(gifViewContainer.snp.top)
        }
        
        addMessageButton.snp.makeConstraints {
            $0.edges.equalTo(messageTextField)
        }
    }
    
    private func configureField(field: inout CoinExchangeSwapTextFieldView, error: String?) {
        field.errorFieldView.isHidden = error == nil
        field.setupErrorField(errorText: error)
        field.amountTextField.textColor = error == nil ? .black : .errorRed
        
        field.snp.remakeConstraints{
            $0.left.right.top.equalToSuperview()
            $0.height.equalTo(error == nil ? 120 : 136)
        }
    }
}

extension Reactive where Base == CoinSendGiftFormView {
    //MARK: - From coin
    var fromCoin: Binder<CustomCoinType> {
        return base.coinAmountTextFieldView.rx.сoin
    }
    
    var fromCoinAmountText: ControlProperty<String?> {
        return base.coinAmountTextFieldView.rx.coinAmountText
    }
    
    var willChangeFromCoinType: Driver<CustomCoinType> {
        return base.coinAmountTextFieldView.rx.willCointTypeChanged
    }
    
    var selectFromPickerItem: Driver<CustomCoinType> {
        return base.coinAmountTextFieldView.rx.selectPickerItem
    }
    
    var maxFromTap: Driver<Void> {
        return base.coinAmountTextFieldView.rx.maxTap
    }
    
    var messageText: ControlProperty<String?> {
        return base.messageTextField.rx.text
    }
    
    var messageErrorText: Binder<String?> {
        return Binder(base) { target, value in
            target.messageTextFieldController?.setErrorText(value, errorAccessibilityValue: value)
        }
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

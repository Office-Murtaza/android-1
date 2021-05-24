import UIKit
import SnapKit

typealias P2POutlinedTextFieldCallback = (String) -> Void

class P2POutlinedTextField: UIView {

    
    private let placeholderLabel = UILabel()
    private let containerView = UIView()
    let textField = UITextField()
    private let measurementLabel = UILabel()
    private var gestureRecognizer: UITapGestureRecognizer?
    private lazy var horizontalStack: UIStackView = {
        let stack = UIStackView()
        stack.distribution = .fillProportionally
        stack.axis = .horizontal
        return stack
    }()
    private var callback: P2POutlinedTextFieldCallback?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func tapAction() {
        textField.becomeFirstResponder()
    }
    
    func update(callback: @escaping P2POutlinedTextFieldCallback) {
        self.callback = callback
    }
    
    private func setupUI() {
    
        self.gestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(tapAction))
        if let recognizer = gestureRecognizer {
            self.addGestureRecognizer(recognizer)
        }
        
        containerView.layer.borderWidth = 1 / UIScreen.main.scale
        containerView.layer.borderColor = UIColor.lightGray.cgColor
        containerView.layer.cornerRadius = 3
        containerView.layer.masksToBounds = true
        
        placeholderLabel.backgroundColor = .white
        placeholderLabel.font = .systemFont(ofSize: 12)
        containerView.addSubviews([
            horizontalStack
        ])
        
        horizontalStack.addArrangedSubviews([
            textField,
            measurementLabel
        ])
        
        textField.font = .systemFont(ofSize: 16)
        
        addSubviews([
            containerView,
            placeholderLabel
        ])

        textField.addTarget(self, action: #selector(textChanged(_:)), for: .editingChanged)
    }
    
    @objc func textChanged(_ textField: UITextField) {
        guard let text = textField.text else { return }
        callback?(text)
    }
    
    private func setupLayout() {
        containerView.snp.makeConstraints {
            $0.left.bottom.right.equalToSuperview()
            $0.top.equalToSuperview().offset(10)
        }
        
        placeholderLabel.snp.makeConstraints {
            $0.left.equalToSuperview().offset(12)
            $0.height.equalTo(15)
        }
        
        horizontalStack.snp.makeConstraints {
            $0.left.equalToSuperview().offset(5)
            $0.top.right.bottom.equalToSuperview()
        }
    }
    
    func setup(placeholder: String, attributedText: NSAttributedString , userInteractionEnabled: Bool = true) {
        placeholderLabel.text = placeholder
        textField.isUserInteractionEnabled = userInteractionEnabled
        textField.attributedText = attributedText
    }
    
    func update(userInteractionEnabled: Bool, keyboardType: UIKeyboardType) {
        textField.isUserInteractionEnabled = userInteractionEnabled
        textField.keyboardType = keyboardType
      
    }
    
    func update(measurmentValue: String) {
        measurementLabel.text = measurmentValue
    }
    
    func update(attributedText: NSAttributedString) {
        textField.attributedText = attributedText
    }
}

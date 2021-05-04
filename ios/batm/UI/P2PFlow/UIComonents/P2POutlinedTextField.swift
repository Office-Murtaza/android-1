import UIKit
import SnapKit

class P2POutlinedTextField: UIView {

    
    private let placeholderLabel = UILabel()
    private let containerView = UIView()
    private let textField = UITextField()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        containerView.layer.borderWidth = 1 / UIScreen.main.scale
        containerView.layer.borderColor = UIColor.lightGray.cgColor
        containerView.layer.cornerRadius = 3
        containerView.layer.masksToBounds = true
        
        placeholderLabel.backgroundColor = .white
        placeholderLabel.font = .systemFont(ofSize: 12)
        
        containerView.addSubview(textField)
        
        textField.font = .systemFont(ofSize: 16)
        
        addSubviews([
            containerView,
            placeholderLabel
        ])
        
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
        
        textField.snp.makeConstraints {
            $0.top.equalToSuperview().offset(5)
            $0.left.equalToSuperview().offset(16)
            $0.right.equalToSuperview().offset(-16)
            $0.bottom.equalToSuperview().offset(-5)
        }
    }
    
    func setup(placeholder: String, attributedText: NSAttributedString , userInteractionEnabled: Bool = true) {
        placeholderLabel.text = placeholder
        textField.isUserInteractionEnabled = userInteractionEnabled
        textField.attributedText = attributedText
    }
    
    func update(attributedText: NSAttributedString) {
        textField.attributedText = attributedText
    }
}

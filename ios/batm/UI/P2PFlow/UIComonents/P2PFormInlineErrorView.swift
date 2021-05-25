import UIKit

class P2PFormInlineErrorView: UIView {

    lazy var errorImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage(named: "swap_error")
        return imageView
    }()
    
    lazy var errorLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = UIColor(hexString: "#B00020")
        return label
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
            errorImageView,
            errorLabel
        ])
    }
    
    private func setupLayout() {
        errorImageView.snp.makeConstraints {
            $0.centerY.equalToSuperview()
            $0.left.equalToSuperview()
        }
        
        errorLabel.snp.makeConstraints {
            $0.centerY.equalToSuperview()
            $0.left.equalTo(errorImageView.snp.right).offset(5)
        }
    }
}

extension P2PFormInlineErrorView: P2PCreateTradeVlidatorDelegate {
    func showErrorMessage(_ message: String) {
        isHidden = false
        errorLabel.text = message
    }
    
    func hideError() {
        errorLabel.text = nil
        self.isHidden = true
    }
}

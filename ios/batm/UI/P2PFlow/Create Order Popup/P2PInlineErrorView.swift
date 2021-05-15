
import UIKit
import SnapKit

class P2PInlineErrorView: UIView {
    
    private lazy var errorImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage(named: "swap_error")
        return imageView
    }()
    
    private lazy var errorLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = UIColor(hexString: "#B00020")
        label.numberOfLines = 0
        return label
    }()
    
    func update(isHidden: Bool,  message: String? = nil) {
        errorImageView.isHidden = isHidden
        errorLabel.isHidden = isHidden
        errorLabel.text = message
    }
    
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
            $0.left.equalToSuperview().offset(15)
        }

        errorLabel.snp.makeConstraints {
            $0.centerY.equalToSuperview()
            $0.left.equalTo(errorImageView.snp.right).offset(5)
            $0.right.equalToSuperview().offset(-15)
        }
    }
}

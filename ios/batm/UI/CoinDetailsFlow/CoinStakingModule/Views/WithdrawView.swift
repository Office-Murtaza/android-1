import UIKit

class WithdrawView: UIView {
    private lazy var imageView = UIImageView()
    
    private lazy var label: UILabel = {
        let label = UILabel()
        label.textColor = .ceruleanBlue
        return label
    }()
    
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView(arrangedSubviews: [imageView, label])
        stackView.axis = .horizontal
        stackView.alignment = .center
        stackView.spacing = 4
        return stackView
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(with image: UIImage?, description: String) {
        imageView.image = image
        label.text = description
        
        if #available(iOS 13.0, *) {
            imageView.image?.withTintColor(.ceruleanBlue)
        } else {
            imageView.tintColor = .ceruleanBlue
        }
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        addSubview(stackView)
    }
    
    private func setupLayout() {
        imageView.snp.makeConstraints {
            $0.size.equalTo(24)
        }
        stackView.snp.makeConstraints {
            $0.centerX.equalToSuperview()
            $0.height.equalToSuperview()
        }
    }
}

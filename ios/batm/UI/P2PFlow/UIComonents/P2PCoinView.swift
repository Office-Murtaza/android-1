import UIKit

class P2PCoinView: UIView {
    
    var coinType: CustomCoinType?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func update(coin: CustomCoinType?) {
        coinType = coin
        imageView.image = coin?.smallLogo
        titleLabel.text = coin?.code
    }
    
    private lazy var stackView: UIStackView = {
        let stack = UIStackView()
        addSubview(stack)
        stack.axis = .horizontal
        stack.distribution = .fillProportionally
        stack.spacing = 5
        return stack
    }()
    
    private  lazy var imageView: UIImageView = {
        let image = UIImageView()
        return image
    }()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 22, weight: .bold)
        return label
    }()
    
    
    private func setupUI() {
        stackView.addArrangedSubviews([imageView, titleLabel])
    }
    
    private func setupLayout() {
        stackView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    }
}

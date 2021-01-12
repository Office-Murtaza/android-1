import UIKit

class SwapUsdValueView: UIView {
    
    private lazy var usdTitleLabel: UILabel = {
        let label = UILabel()
        label.textAlignment = .right
        label.textColor = .slateGrey
        label.font = .systemFont(ofSize: 16, weight: .regular)
        
        return label
    }()
    
    private lazy var usdValueLabel: UILabel = {
        let label = UILabel()
        label.textAlignment = .right
        label.textColor = .black
        label.font = .systemFont(ofSize: 20, weight: .semibold)
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
        configure(value: 0)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupUI() {
        self.addSubviews([
            usdTitleLabel,
            usdValueLabel
        ])
    }
    
    func setupLayout() {
        
        usdTitleLabel.snp.makeConstraints {
            $0.right.equalToSuperview().offset(-15)
            $0.left.equalToSuperview()
            $0.top.equalToSuperview()
            $0.height.equalTo(20)
        }
        
        usdValueLabel.snp.makeConstraints{
            $0.right.equalToSuperview().offset(-15)
            $0.left.equalToSuperview()
            $0.top.equalTo(usdTitleLabel.snp.bottom).offset(5)
            $0.bottom.equalToSuperview()
        }
        
    }
    
    func configure(value: Decimal) {
        usdTitleLabel.text = localize(L.Swap.Usd.title)
        usdValueLabel.text = "$ \(value.coinFormatted)"
    }
    
}

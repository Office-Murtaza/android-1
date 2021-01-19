import UIKit

class CoinStakingInfoView: UIView {
    var isSeparatorHidden: Bool? {
        didSet {
            verticalSeparatorView.isHidden = isSeparatorHidden.value
        }
    }
    
    private lazy var leftInfoLabel: UILabel = {
        let label = UILabel()
        label.textColor = .slateGrey
        label.textAlignment = .left
        label.font = .systemFont(ofSize: 12, weight: .regular)
        return label
    }()
    private lazy var leftAmountLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.textAlignment = .left
        label.numberOfLines = 0
        label.font = .systemFont(ofSize: 16, weight: .semibold)
        return label
    }()
    private lazy var rightInfoLabel: UILabel = {
        let label = UILabel()
        label.textColor = .slateGrey
        label.textAlignment = .right
        label.font = .systemFont(ofSize: 12, weight: .regular)
        return label
    }()
    private lazy var rightAmountLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.numberOfLines = 0
        label.textAlignment = .right
        label.font = .systemFont(ofSize: 16, weight: .semibold)
        return label
    }()
    private lazy var verticalSeparatorView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hexString: "212121", alpha: 0.08)
        return view
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configureLeftView(with title: String? = nil, value: String? = nil, valueColor: UIColor = .black) {
        leftInfoLabel.text = title
        leftAmountLabel.text = value
        leftAmountLabel.textColor = valueColor
    }
    
    func configureRightView(with title: String? = nil, value: String? = nil, valueColor: UIColor = .black) {
        rightInfoLabel.text = title
        rightAmountLabel.text = value
        rightAmountLabel.textColor = valueColor
        
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        
        addSubviews(leftInfoLabel,
                    leftAmountLabel,
                    verticalSeparatorView,
                    rightInfoLabel,
                    rightAmountLabel)
    }
    
    private func setupLayout() {
        leftInfoLabel.snp.makeConstraints {
            $0.top.equalToSuperview().offset(0)
            $0.left.equalToSuperview().offset(15)
            $0.right.equalTo(verticalSeparatorView.snp.left).offset(0)
            $0.bottom.equalTo(leftAmountLabel.snp.top).offset(-2)
        }
        
        leftAmountLabel.snp.makeConstraints {
            $0.top.equalTo(leftInfoLabel.snp.bottom).offset(2)
            $0.left.equalToSuperview().offset(15)
            $0.right.equalTo(verticalSeparatorView.snp.left).offset(0)
            $0.bottom.equalToSuperview().offset(0)
        }
        
        verticalSeparatorView.snp.makeConstraints {
            $0.centerX.equalToSuperview()
            $0.width.equalTo(1)
            $0.top.bottom.equalToSuperview().offset(0)
        }
        
        rightInfoLabel.snp.makeConstraints {
            $0.top.equalToSuperview().offset(0)
            $0.left.equalTo(verticalSeparatorView.snp.right).offset(0)
            $0.right.equalToSuperview().offset(-15)
            $0.bottom.equalTo(rightAmountLabel.snp.top).offset(-2)
        }
        
        rightAmountLabel.snp.makeConstraints {
            $0.top.equalTo(rightInfoLabel.snp.bottom).offset(2)
            $0.left.equalTo(verticalSeparatorView.snp.right).offset(0)
            $0.right.equalToSuperview().offset(-15)
            $0.bottom.equalToSuperview().offset(0)
        }
    }
}

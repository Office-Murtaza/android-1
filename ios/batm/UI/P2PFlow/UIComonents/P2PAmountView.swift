import UIKit

class P2PAmountView: UIView {

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private lazy var headerLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = UIColor.black.withAlphaComponent(0.6)
        return label
    }()

    private lazy var valueLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .bold)
        label.textColor = UIColor(hexString: "#212121")
        
        return label
    }()

    private func setupUI() {
        addSubviews([
            headerLabel,
            valueLabel
        ])
    }

    private func setupLayout() {
        headerLabel.snp.makeConstraints {
            $0.top.equalToSuperview()
            $0.left.equalToSuperview()
            $0.right.equalToSuperview()
            $0.height.equalTo(20)
        }
        
        valueLabel.snp.makeConstraints{
            $0.top.equalTo(headerLabel.snp.bottom).offset(2)
            $0.left.equalToSuperview()
            $0.right.equalToSuperview()
            $0.bottom.equalToSuperview()
        }
    }
    
    func update(title: String, value: String, textAlignMent: NSTextAlignment) {
        headerLabel.text = title
        valueLabel.text = value
        headerLabel.textAlignment = textAlignMent
        valueLabel.textAlignment = textAlignMent
    }
}

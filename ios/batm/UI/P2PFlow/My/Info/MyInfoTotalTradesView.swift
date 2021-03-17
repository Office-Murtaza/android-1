import UIKit

class MyInfoTotalTradesView: UIView {
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
        label.text = "Total trades"
        return label
    }()

    private lazy var valueLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 24, weight: .regular)
        label.textColor = .black
        
        return label
    }()
    
    private func setupUI() {
        backgroundColor = UIColor.lightGray.withAlphaComponent(0.1)
        addSubviews([
            headerLabel,
            valueLabel
        ])
    }
    
    private func setupLayout() {
        headerLabel.snp.makeConstraints {
            $0.top.equalToSuperview().offset(16)
            $0.left.equalToSuperview().offset(16)
        }
        
        valueLabel.snp.makeConstraints {
            $0.bottom.equalToSuperview().offset(-5)
            $0.left.equalToSuperview().offset(16)
        }
    }

    func update(value: String) {
        valueLabel.text = value
    }
}

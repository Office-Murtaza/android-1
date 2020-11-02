import UIKit

class StatusView: UIView {
    
    let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 11, weight: .bold)
        label.adjustsFontSizeToFitWidth = true
        label.minimumScaleFactor = 0.7
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setupUI()
        setupLayout()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(text: String, color: UIColor) {
        titleLabel.text = text
        titleLabel.textColor = color
        layer.borderColor = color.cgColor
        backgroundColor = color.withAlphaComponent(0.05)
    }
    
    func configureStaking(text: String, background: UIColor, borderColor: UIColor, fontColor: UIColor) {
        titleLabel.text = text
        titleLabel.textColor = fontColor
        layer.borderColor = borderColor.cgColor
        backgroundColor = background.withAlphaComponent(0.05)
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        
        layer.borderWidth = 1
        layer.cornerRadius = 3
        
        addSubview(titleLabel)
    }
    
    private func setupLayout() {
        titleLabel.snp.makeConstraints {
            $0.top.bottom.equalToSuperview().inset(4)
            $0.left.right.equalToSuperview().inset(5)
        }
    }
}

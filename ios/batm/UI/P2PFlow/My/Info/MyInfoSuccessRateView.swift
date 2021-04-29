import UIKit
import SnapKit

class MyInfoSuccessRateView: UIView {

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
      label.text = localize(L.P2p.Success.Rate.title)
        return label
    }()

    private lazy var valueLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 24, weight: .regular)
        label.textColor = .black
        
        return label
    }()
    
    private lazy var starImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        imageView.image = UIImage(named: "p2p_info_rate_star")
        return imageView
    }()
    
    private func setupUI() {
        backgroundColor = UIColor.lightGray.withAlphaComponent(0.1)
        addSubviews([
            headerLabel,
            valueLabel,
            starImageView
        ])
    }
    
    private func setupLayout() {
        headerLabel.snp.makeConstraints {
            $0.top.equalToSuperview().offset(16)
            $0.left.equalToSuperview().offset(16)
        }
        
        starImageView.snp.makeConstraints {
            $0.bottom.equalToSuperview().offset(-12)
            $0.left.equalTo(headerLabel)
        }
        
        valueLabel.snp.makeConstraints {
            $0.bottom.equalToSuperview().offset(-5)
            $0.left.equalTo(starImageView.snp.right).offset(5)
        }
    }
    
    func update(value: String) {
        valueLabel.text = value
    }
    
}

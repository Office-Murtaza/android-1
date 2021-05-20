import UIKit
import SnapKit

class P2POrderDetailsScoreView: UIView {
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14)
        label.textColor = UIColor(hexString: "#58585A")
        return label
    }()
    
    private lazy var scoreImageView: UIImageView = {
        let image = UIImageView()
        image.contentMode = .scaleAspectFit
        image.image = UIImage(named: "p2p_star")
        return image
    }()
    
    private lazy var scoreValueLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .bold)
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
    
    func setup(title: String, score: String) {
        titleLabel.text = title
        scoreValueLabel.text = score
    }
    
    private func setupUI() {
        addSubviews([
            titleLabel,
            scoreImageView,
            scoreValueLabel
        ])
    }
    private func setupLayout() {
        titleLabel.snp.makeConstraints {
            $0.centerY.equalToSuperview()
            $0.left.equalToSuperview().offset(15)
        }
        
        scoreValueLabel.snp.makeConstraints {
            $0.right.equalToSuperview().offset(-15)
            $0.centerY.equalToSuperview()
        }
        
        scoreImageView.snp.makeConstraints {
            $0.centerY.equalToSuperview()
            $0.right.equalTo(scoreValueLabel.snp.left).offset(-5)
            $0.width.height.equalTo(15)
        }
    }
    
}

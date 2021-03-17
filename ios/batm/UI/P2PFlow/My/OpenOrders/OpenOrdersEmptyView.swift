import UIKit
import SnapKit

class OpenOrdersEmptyView: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private lazy var imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        imageView.image = UIImage(named: "p2p_open_orders_empty_info")
        return imageView
    }()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .regular)
        label.text = "There isn't open orders yet."
        label.textAlignment = .center
        return label
    }()
    
    
    private func setupUI() {
        addSubviews([
            imageView,
            titleLabel
        ])
    }
    
    private func setupLayout() {
        titleLabel.snp.makeConstraints {
            $0.centerY.equalToSuperview()
            $0.left.equalToSuperview().offset(15)
            $0.right.equalToSuperview().offset(-15)
        }
        
        imageView.snp.makeConstraints {
            $0.bottom.equalTo(titleLabel.snp.top).offset(-18)
            $0.centerX.equalToSuperview()
            $0.width.height.equalTo(20)
        }
    }
}

import UIKit
import SnapKit

class P2POrderDetailsStatusView: UIView {

    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14)
        label.textColor = UIColor(hexString: "#58585A")
        label.text = localize(L.P2p.Order.Details.status)
        return label
    }()
    
    private lazy var statusImageView: UIImageView = {
        let imageView = UIImageView()
        return imageView
    }()

    private lazy var statusLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .bold)
        label.textColor = .black
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
    
    func update(status: TradeOrderStatus) {
        statusImageView.image = status.image
        statusLabel.text = status.title
    }
    
    private func setupUI() {
        addSubviews([
            titleLabel,
            statusImageView,
            statusLabel
        ])
    }
    
    private func setupLayout() {
        titleLabel.snp.makeConstraints {
            $0.left.equalToSuperview().offset(15)
            $0.centerY.equalToSuperview()
        }

        statusImageView.snp.makeConstraints {
            $0.centerY.equalToSuperview()
            $0.right.equalToSuperview().offset(-15)
            $0.width.height.equalTo(20)
        }

        statusLabel.snp.makeConstraints {
            $0.right.equalTo(statusImageView.snp.left).offset(-5)
            $0.centerY.equalToSuperview()
        }
    }
}

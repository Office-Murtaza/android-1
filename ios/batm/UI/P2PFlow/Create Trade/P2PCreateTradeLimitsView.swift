import UIKit

class P2PCreateTradeLimitsView: P2PDistanceRangeView {

    lazy var feeLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = UIColor(hexString: "#000000", alpha: 0.6)
        label.text = "0"
        return label
    }()
    
    override func setupUI() {
        super.setupUI()
        addSubview(feeLabel)
    }
    
    override func setupLayout() {
        super.setupLayout()
        feeLabel.snp.makeConstraints {
            $0.top.equalTo(toField.snp.bottom).offset(10)
            $0.right.equalTo(toField.snp.right)
        }
    }

}

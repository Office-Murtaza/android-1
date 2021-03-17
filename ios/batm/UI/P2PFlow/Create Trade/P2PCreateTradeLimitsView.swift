import UIKit

class P2PCreateTradeLimitsView: P2PDistanceRangeView {

    
    lazy var errorImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage(named: "swap_error")
        return imageView
    }()
    
    lazy var errorLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = UIColor(hexString: "#B00020")
        return label
    }()
    
    lazy var feeLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = UIColor(hexString: "#000000", alpha: 0.6)
        return label
    }()
    
    override func setupUI() {
        super.setupUI()
        
        addSubviews([
            errorImageView,
            errorLabel,
            feeLabel
        ])
        
        //TEST DATA
        errorLabel.text = "test validation"
        feeLabel.text = "fee"
        
    }
    
    override func setupLayout() {
        super.setupLayout()
        
        errorImageView.snp.makeConstraints {
            $0.top.equalTo(fromField.snp.bottom).offset(10)
            $0.left.equalTo(fromField.snp.left)
        }
        
        errorLabel.snp.makeConstraints {
            $0.top.equalTo(errorImageView.snp.top)
            $0.left.equalTo(errorImageView.snp.right).offset(5)
        }
        
        feeLabel.snp.makeConstraints {
            $0.top.equalTo(toField.snp.bottom).offset(10)
            $0.right.equalTo(toField.snp.right)
        }
    }

}

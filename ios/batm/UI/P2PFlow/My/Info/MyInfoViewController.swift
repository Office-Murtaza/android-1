import UIKit
import SnapKit

class MyInfoViewController: UIViewController {

    private lazy var userImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        imageView.image = UIImage(named: "p2p_info_user_icon")
        return imageView
    }()
    
    private lazy var idTitleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .regular)
        return label
    }()
    
    private lazy var statusImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private lazy var statusLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .regular)
        return label
    }()
    
    private let rateView = MyInfoSuccessRateView()
    private let totalInfo = MyInfoTotalTradesView()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupLayout()
    }

    private func setupUI() {
        view.addSubviews([
            userImageView,
            idTitleLabel,
            statusImageView,
            statusLabel,
            rateView,
            totalInfo
        ])
    }
    
    private func setupLayout() {
        userImageView.snp.makeConstraints {
            $0.top.equalToSuperview().offset(30)
            $0.left.equalToSuperview().offset(19)
            $0.height.width.equalTo(40)
        }
        
        idTitleLabel.snp.makeConstraints {
            $0.top.equalTo(userImageView)
            $0.left.equalTo(userImageView.snp.right).offset(12)
        }
        
        statusImageView.snp.makeConstraints {
            $0.top.equalTo(idTitleLabel.snp.bottom).offset(3)
            $0.left.equalTo(idTitleLabel)
        }
        
        statusLabel.snp.makeConstraints {
            $0.top.equalTo(statusImageView)
            $0.left.equalTo(statusImageView.snp.right).offset(3)
        }
        
        rateView.snp.makeConstraints {
            $0.top.equalTo(userImageView.snp.bottom).offset(28)
            $0.left.equalToSuperview().offset(15)
            $0.height.equalTo(80)
            $0.right.equalTo(view.snp.centerX).offset(-1)
        }
        
        totalInfo.snp.makeConstraints {
            $0.top.equalTo(rateView)
            $0.left.equalTo(rateView.snp.right).offset(1)
            $0.right.equalToSuperview().offset(-15)
            $0.height.equalTo(80)
        }
        
    }
    
    func update(id: String,
                verificationImage: UIImage?,
                verificationStatus: String,
                rate: String,
                total: String) {
        idTitleLabel.text = id
        statusImageView.image = verificationImage
        statusLabel.text = verificationStatus
        rateView.update(value: rate)
        totalInfo.update(value: total)
    }
}

import UIKit
import SnapKit

protocol P2POrderDetailsIdViewDelegate: AnyObject {
    func didSelectedCopy(id: String)
}

class P2POrderDetailsIdView: UIView {
    
    weak var delegate: P2POrderDetailsIdViewDelegate?
    
    private lazy var idLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .bold)
        return label
    }()
    
    private lazy var idTitleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14)
        label.text = "ID"
        return label
    }()
    
    private lazy var clipBoardButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage(named: "clipboard"), for: .normal)
        button.addTarget(self, action: #selector(didTapCopy), for: .touchUpInside)
        return button
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func didTapCopy() {
        delegate?.didSelectedCopy(id: idLabel.text ?? "")
    }
    
    private func setupUI() {
        addSubviews([
            idLabel,
            idTitleLabel,
            clipBoardButton
        ])
    }
    
    private func setupLayout() {
        idTitleLabel.snp.makeConstraints {
            $0.centerY.equalToSuperview()
            $0.left.equalToSuperview().offset(15)
        }
        
        idLabel.snp.makeConstraints {
            $0.centerY.equalTo(idTitleLabel.snp.centerY)
            $0.left.equalTo(idTitleLabel.snp.right).offset(8)
            $0.right.greaterThanOrEqualTo(clipBoardButton.snp.left)
        }
        
        clipBoardButton.snp.makeConstraints {
            $0.centerY.equalTo(idLabel.snp.centerY)
            $0.right.equalToSuperview().offset(-15)
        }
    }
    
    func setup(id: String) {
        idLabel.text = id
    }
}

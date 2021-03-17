import UIKit
import SnapKit
import MaterialComponents

protocol MyTradesEmptyViewDelegate: class {
    func didTapCreateTrade()
}

class MyTradesEmptyView: UIView {

    weak var delegate: MyTradesEmptyViewDelegate?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private lazy var infoLabel: UILabel = {
        let label = UILabel()
        label.text = "You haven't created a single trade yet."
        label.font = .systemFont(ofSize: 16, weight: .regular)
        return label
    }()
    
    private lazy var createTradeButton: MDCButton = {
        let button = MDCButton.contained
        button.setTitle("Create new Trade", for: .normal)
        button.addTarget(self, action: #selector(crateTrade), for: .touchUpInside)
        return button
    }()
    
    func setupUI() {
        addSubviews([
            infoLabel,
            createTradeButton
        ])
    }
    
    func setupLayout() {
        infoLabel.snp.makeConstraints {
            $0.centerX.equalToSuperview()
            $0.centerY.equalToSuperview()
        }
        
        createTradeButton.snp.makeConstraints {
            $0.top.equalTo(infoLabel.snp.bottom).offset(32)
            $0.left.equalToSuperview().offset(16)
            $0.right.equalToSuperview().offset(-16)
            $0.height.equalTo(48)
        }
    }
    
    @objc func crateTrade() {
        delegate?.didTapCreateTrade()
    }
}

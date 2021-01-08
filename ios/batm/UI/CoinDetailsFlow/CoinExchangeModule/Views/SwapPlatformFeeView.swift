import UIKit

class SwapPlatformFeeView: UIView {
    
    private lazy var feeLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = .slateGrey
        label.textAlignment = .left
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
    
    private func setupUI() {
        
        addSubview(feeLabel)
    }
    
    private func setupLayout() {

        feeLabel.snp.makeConstraints{
            $0.top.bottom.equalToSuperview()
            $0.centerX.equalTo(self.snp.centerX)
        }
    }
    
    func configure(fee: String) {
        feeLabel.text = "\(localize(L.Swap.platformfee)) \(fee)"
    }

}

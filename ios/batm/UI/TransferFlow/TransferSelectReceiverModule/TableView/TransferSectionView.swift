import UIKit

class TransferSectionView: UIView {
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupUI() {
        backgroundColor = .white
        addSubviews([
            title,
            separatorView
        ])
    }
    
    func setupLayout() {
        title.snp.makeConstraints{
            $0.left.equalToSuperview().offset(15)
            $0.top.equalToSuperview().offset(15)
            $0.bottom.equalToSuperview().offset(-15)
        }
        
        separatorView.snp.makeConstraints{
            $0.left.equalTo(title.snp.left)
            $0.bottom.equalToSuperview()
            $0.height.equalTo(1/UIScreen.main.scale)
            $0.right.equalToSuperview().offset(-15)
        }
    }
    
    lazy var title: UILabel = {
        let label = UILabel()
        return label
    }()
    
    lazy var separatorView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hexString: "#14212121")
        return view
    }()
    
}

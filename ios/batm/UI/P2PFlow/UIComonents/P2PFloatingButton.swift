import UIKit

protocol P2PFloatingButtonDelegate: AnyObject {
    func didTapFloadingButton()
}

class P2PFloatingButton: UIView {
    
    weak var delegate: P2PFloatingButtonDelegate?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    init(image: UIImage?) {
        super.init(frame: .zero)
        setupUI()
        setupLayout()
        floatingButton.setImage(image, for: .normal)
    }
    
    private lazy var floatingButton: UIButton = {
        let button = UIButton()
        button.addTarget(self, action: #selector(floatingButtonAction), for: .touchUpInside)
        return button
    }()
    
    private func setupUI() {
        backgroundColor = UIColor(hexString: "0073E4")
        addSubview(floatingButton)
    }
    
    private func setupLayout() {
        floatingButton.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    }
    
    @objc func floatingButtonAction() {
        delegate?.didTapFloadingButton()
    }
    

}

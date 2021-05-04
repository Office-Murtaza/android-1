import UIKit

class P2PResetAllView: UIView, P2PFloatingButtonDelegate {
    
    typealias ResetAllViewAction = () -> Void
    
    private var reset: ResetAllViewAction?
    private var apply: ResetAllViewAction?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private lazy var resetButton: UIButton = {
        let button = UIButton()
        button.setTitle("RESET ALL", for: .normal)
        button.addTarget(self, action: #selector(resetAll), for: .touchUpInside)
        button.setTitleColor(UIColor(hexString: "0073E4"), for: .normal)
        return button
    }()
    
    private let applyFilterButton = P2PFloatingButton(image: UIImage(named: "p2p_apply_filter"))
    
    private func setupUI() {
        applyFilterButton.layer.cornerRadius = 28
        applyFilterButton.layer.masksToBounds = false
        applyFilterButton.layer.shadowColor = UIColor.black.cgColor
        applyFilterButton.layer.shadowOpacity = 0.3
        applyFilterButton.layer.shadowOffset = CGSize(width: 1, height: 2)
        applyFilterButton.delegate = self
        
        addSubviews([
            resetButton,
            applyFilterButton
        ])
    }
    
    private func setupLayout() {
        resetButton.snp.makeConstraints {
            $0.top.bottom.equalToSuperview()
            $0.right.equalToSuperview().offset(-106)
        }
        
        applyFilterButton.snp.makeConstraints {
            $0.width.height.equalTo(56)
            $0.right.equalToSuperview().offset(-18)
            $0.bottom.equalToSuperview().offset(-18)
        }
        
    }
    
    @objc func resetAll() {
        reset?()
    }
    
    func didTapFloadingButton() {
        apply?()
    }
    
    func didTapApplyFilter(_ apply: @escaping ResetAllViewAction, didTapReset: @escaping ResetAllViewAction) {
        self.apply = apply
        self.reset = didTapReset
    }
}

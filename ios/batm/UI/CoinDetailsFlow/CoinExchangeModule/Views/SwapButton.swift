import UIKit

class SwapButton: UIButton {
    
    override init(frame: CGRect){
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        setImage(UIImage(named: "swap_vert"), for: .normal)
        backgroundColor = .white
        layer.borderColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.12).cgColor
        layer.borderWidth = 2
        layer.cornerRadius = 18
        layer.masksToBounds = true
    }
}

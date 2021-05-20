import UIKit
import SnapKit

class P2POrderDetailsAmountView: UIView {
    
    lazy var cryptoAmountTitle: UILabel = {
      let label = UILabel()
      label.font = .systemFont(ofSize: 12)
      label.textColor = UIColor.black.withAlphaComponent(0.6)
      return label
    }()
    
    lazy var cryptoAmountValue: UILabel = {
      let label = UILabel()
      label.font = .systemFont(ofSize: 22)
      label.textColor = UIColor.black
      return label
    }()
    
    lazy var fiatAmountTitle: UILabel = {
      let label = UILabel()
      label.font = .systemFont(ofSize: 12)
      label.textColor = UIColor.black.withAlphaComponent(0.6)
      return label
    }()
    
    var fiatAmountValue: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 22)
        label.textColor = UIColor.black
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
      
      addSubviews([
        cryptoAmountTitle,
        cryptoAmountValue,
        fiatAmountTitle,
        fiatAmountValue
      ])
      
      cryptoAmountTitle.text = localize(L.P2p.Crypto.Amount.title)
      fiatAmountTitle.text = localize(L.P2p.Fiat.Amount.title)
      
      cryptoAmountValue.text = "0"
      fiatAmountValue.text = "0"
    }
    
    func update(cryptoAmount: String, fiatAmount: String) {
        cryptoAmountValue.text = cryptoAmount
        fiatAmountValue.text = fiatAmount
    }
    
    private func setupLayout() {
      cryptoAmountTitle.snp.makeConstraints {
        $0.top.equalToSuperview().offset(24)
        $0.left.equalToSuperview().offset(15)
      }
      
      cryptoAmountValue.snp.makeConstraints {
        $0.top.equalTo(cryptoAmountTitle.snp.bottom).offset(8)
        $0.left.equalTo(cryptoAmountTitle.snp.left)
      }
      
      fiatAmountTitle.snp.makeConstraints {
        $0.right.equalToSuperview().offset(-15)
        $0.top.equalTo(cryptoAmountTitle.snp.top)
      }
     
        fiatAmountValue.snp.makeConstraints {
        $0.top.equalTo(fiatAmountTitle.snp.bottom)
        $0.right.equalToSuperview().offset(-15)
      }
      
    }
    
    
}

import UIKit
import RxSwift
import RxCocoa

class P2PSelectCoinView: CoinExchangeSwapTextFieldView {
    
    lazy var priceLabel: UILabel = {
        let label = UILabel()
        label.textColor = .slateGrey
        label.font = .systemFont(ofSize: 12, weight: .regular)
        return label
    }()
    
    override func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        addSubviews(coinTextField,
                    fakeToCoinTextField,
                    coinTypeImageView,
                    balanceLabel,
                    amountTextField,
                    resultLabel,
                    errorFieldView,
                    feeLabel,
                    priceLabel)
        coinTextField.font = .systemFont(ofSize: 22, weight: .bold)
        priceLabel.text = "Price"
    }
    
    override func setupLayout() {
        coinTypeImageView.snp.makeConstraints {
            $0.top.equalToSuperview().offset(32)
            $0.left.equalToSuperview().offset(15)
            $0.width.equalTo(32)
            $0.height.equalTo(32)
        }
        
        balanceLabel.snp.makeConstraints {
            $0.top.equalTo(coinTypeImageView.snp.bottom).offset(10)
            $0.left.equalTo(coinTypeImageView)
        }
        
        feeLabel.snp.makeConstraints {
            $0.top.equalTo(balanceLabel.snp.bottom).offset(5)
            $0.left.equalTo(balanceLabel.snp.left)
        }
                
        coinTextField.snp.makeConstraints {
            $0.centerY.equalTo(coinTypeImageView.snp_centerYWithinMargins)
            $0.left.equalTo(coinTypeImageView.snp_rightMargin).offset(20)
            $0.height.equalTo(32)
            $0.width.greaterThanOrEqualTo(40)
        }
        
        priceLabel.snp.makeConstraints {
            $0.bottom.equalTo(amountTextField.snp.top)
            $0.top.equalToSuperview().offset(5)
            $0.right.equalToSuperview().offset(-15)
        }
        
        amountTextField.snp.makeConstraints {
            $0.right.equalToSuperview().offset(-15)
            $0.left.equalTo(fakeToCoinTextField.snp.right)
            $0.centerY.equalTo(fakeToCoinTextField)
        }
        
        fakeToCoinTextField.snp.makeConstraints {
            $0.edges.equalTo(coinTextField)
        }
        
        resultLabel.snp.makeConstraints {
            $0.right.equalTo(amountTextField.snp.right)
            $0.centerY.equalTo(balanceLabel)
        }
      
        errorFieldView.snp.makeConstraints {
            $0.top.equalTo(feeLabel.snp.bottom).offset(5)
            $0.right.equalTo(amountTextField.snp.right)
            $0.left.equalTo(balanceLabel.snp.left)
        }
        
        setupPicker()
    }
    
    func setCoinBalance(_ balance: CoinBalance) {
        amountTextField.text = nil
        coinTypeImageView.image = balance.type.mediumLogo
        coinTextField.text = balance.type.code
        balanceLabel.text = "Reserved  \(balance.reservedBalance.coinFormatted.withCoinType(balance.type))"
    }
}



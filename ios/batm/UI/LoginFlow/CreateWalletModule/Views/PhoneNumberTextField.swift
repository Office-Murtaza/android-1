import UIKit
import RxSwift
import RxCocoa
import FlagPhoneNumber

class PhoneNumberTextField: FPNTextField {
  
  let imageView = UIImageView(image: UIImage(named: "create_wallet_phone"))
  
  private let padding = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 35)
  
  init() {
    super.init(frame: .null)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    textColor = .warmGrey
    font = .poppinsMedium12
    flagSize = CGSize(width: 20, height: 20)
    setCountries(including: [FPNCountryCode.US])
    
    layer.cornerRadius = 16
    layer.borderWidth = 1
    layer.borderColor = UIColor.whiteTwo.cgColor
    
    addSubview(imageView)
  }
  
  private func setupLayout() {
    snp.makeConstraints {
      $0.height.equalTo(50)
    }
    imageView.snp.makeConstraints {
      $0.centerY.equalToSuperview()
      $0.right.equalToSuperview().offset(-17)
    }
  }
  
}

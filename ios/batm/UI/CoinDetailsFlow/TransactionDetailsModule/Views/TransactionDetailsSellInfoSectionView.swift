import UIKit
import RxSwift
import RxCocoa
import GiphyUISDK
import GiphyCoreSDK

class TransactionDetailsSellInfoSectionView: UIView, HasDisposeBag {
  
  let qrCodeImageView = UIImageView(image: nil)
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubview(qrCodeImageView)
  }
  
  private func setupLayout() {
    qrCodeImageView.snp.makeConstraints {
      $0.top.bottom.equalToSuperview().inset(35)
      $0.centerX.equalToSuperview()
    }
  }
  
  func configure(for details: TransactionDetails) {
    if let sellInfo = details.sellInfo {
      qrCodeImageView.image = UIImage.qrCode(from: sellInfo)
    }
  }
}

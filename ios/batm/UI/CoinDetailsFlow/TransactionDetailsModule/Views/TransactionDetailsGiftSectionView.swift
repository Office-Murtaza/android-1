import UIKit
import RxSwift
import RxCocoa
import GiphyUISDK
import GiphyCoreSDK
import PhoneNumberKit

class TransactionDetailsGiftSectionView: UIView, HasDisposeBag {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 15
    return stackView
  }()
  
  lazy var txidRowView = TransactionDetailsRowView()
  
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
    
    addSubview(stackView)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.top.bottom.equalToSuperview().inset(30)
      $0.left.right.equalToSuperview()
    }
  }
  
  func configure(for details: TransactionDetails) {
    if let phone = details.phone {
      let phoneView = TransactionDetailsRowView()
      phoneView.configure(for: .text(phone.phoneFormatted), with: localize(L.TransactionDetails.phone))
      stackView.addArrangedSubview(phoneView)
    }
    
    if let imageId = details.imageId {
      let imageView = TransactionDetailsRowView()
      imageView.configure(for: .image(imageId), with: localize(L.TransactionDetails.image))
      stackView.addArrangedSubview(imageView)
    }
    
    if let message = details.message {
      let phoneView = TransactionDetailsRowView()
      phoneView.configure(for: .text(message), with: localize(L.TransactionDetails.message))
      stackView.addArrangedSubview(phoneView)
    }
  }
}

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
  
  private func addTextRow(text: String, title: String) {
    let view = TransactionDetailsRowView()
    view.configure(for: .text(text), with: title)
    stackView.addArrangedSubview(view)
  }
  
  private func addImageRow(id: String, title: String) {
    let view = TransactionDetailsRowView()
    view.configure(for: .image(id), with: title)
    stackView.addArrangedSubview(view)
  }
  
  func configure(for details: TransactionDetails) {
    details.phone.flatMap { addTextRow(text: $0.phoneFormatted, title: localize(L.TransactionDetails.phone)) }
    details.imageId.flatMap { addImageRow(id: $0, title: localize(L.TransactionDetails.image)) }
    details.message.flatMap { addTextRow(text: $0, title: localize(L.TransactionDetails.message)) }
  }
}

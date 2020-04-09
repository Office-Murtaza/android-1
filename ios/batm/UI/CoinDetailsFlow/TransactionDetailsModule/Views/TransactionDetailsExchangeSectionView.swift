import UIKit
import RxSwift
import RxCocoa
import GiphyUISDK
import GiphyCoreSDK
import TrustWalletCore

class TransactionDetailsExchangeSectionView: UIView, HasDisposeBag {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 10
    return stackView
  }()
  
  lazy var refTxIdRowView = TransactionDetailsRowView()
  
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
  
  private func addRefTxIdRow(text: String) {
    refTxIdRowView.configure(for: .link(text), with: localize(L.TransactionDetails.refTxId))
    stackView.addArrangedSubview(refTxIdRowView)
  }
  
  private func addTextRow(text: String, title: String) {
    let view = TransactionDetailsRowView()
    view.configure(for: .text(text), with: title)
    stackView.addArrangedSubview(view)
  }
  
  func configure(for details: TransactionDetails) {
    details.refTxId.flatMap { addRefTxIdRow(text: $0) }
    details.refCoin.flatMap { addTextRow(text: $0.code, title: localize(L.TransactionDetails.refCoin)) }
    details.refCryptoAmount.flatMap { addTextRow(text: $0.coinFormatted, title: localize(L.TransactionDetails.refAmount)) }
  }
}

extension Reactive where Base == TransactionDetailsExchangeSectionView {
  var linkTap: Driver<Void> {
    return base.refTxIdRowView.rx.tap
  }
}

import UIKit
import RxSwift
import RxCocoa
import GiphyUISDK
import GiphyCoreSDK
import TrustWalletCore

class TransactionDetailsGeneralSectionView: UIView, HasDisposeBag {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 10
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
  
  private func addTxIdRow(text: String) {
    txidRowView.configure(for: .link(text), with: localize(L.TransactionDetails.txId))
    stackView.addArrangedSubview(txidRowView)
  }
  
  private func addTextRow(text: String, title: String) {
    let view = TransactionDetailsRowView()
    view.configure(for: .text(text), with: title)
    stackView.addArrangedSubview(view)
  }
  
  private func addStatusRow(status: TransactionStatus) {
    let view = TransactionDetailsRowView()
    view.configure(for: .status(status), with: localize(L.TransactionDetails.status))
    stackView.addArrangedSubview(view)
  }
  
  private func addCashStatusRow(status: TransactionCashStatus) {
    let view = TransactionDetailsRowView()
    view.configure(for: .cashStatus(status), with: localize(L.TransactionDetails.cashStatus))
    stackView.addArrangedSubview(view)
  }
  
  func configure(with details: TransactionDetails, for type: CustomCoinType) {
    details.txId.flatMap { addTxIdRow(text: $0) }
    details.txDbId.flatMap { addTextRow(text: $0, title: localize(L.TransactionDetails.txDbId)) }
    addTextRow(text: details.type.verboseValue, title: localize(L.TransactionDetails.type))
    addStatusRow(status: details.status)
    details.cashStatus.flatMap { addCashStatusRow(status: $0) }
    
    let fiatAmount = details.fiatAmount.flatMap { $0.fiatSellFormatted.withUSD }
    let cryptoAmount = details.cryptoAmount.flatMap { "\($0.coinFormatted) \(type.code)" }
    let cryptoFee = details.cryptoFee.flatMap { "\($0.coinFormatted) \(type.code)" }
    
    fiatAmount.flatMap { addTextRow(text: $0, title: localize(L.TransactionDetails.fiatAmount)) }
    cryptoAmount.flatMap { addTextRow(text: $0, title: localize(L.TransactionDetails.cryptoAmount)) }
    cryptoFee.flatMap { addTextRow(text: $0, title: localize(L.TransactionDetails.fee)) }
    details.dateString.flatMap { addTextRow(text: $0, title: localize(L.TransactionDetails.date)) }
    details.fromAddress.flatMap { addTextRow(text: $0, title: localize(L.TransactionDetails.fromAddress)) }
    details.toAddress.flatMap { addTextRow(text: $0, title: localize(L.TransactionDetails.toAddress)) }
  }
}

extension Reactive where Base == TransactionDetailsGeneralSectionView {
  var linkTap: Driver<Void> {
    return base.txidRowView.rx.tap
  }
}

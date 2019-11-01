import UIKit
import RxSwift
import RxCocoa
import GiphyUISDK
import GiphyCoreSDK

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
  
  func configure(for details: TransactionDetails) {
    txidRowView.configure(for: .link(details.txid), with: localize(L.TransactionDetails.txId))
    stackView.addArrangedSubview(txidRowView)
    
    [(details.type.verboseValue, localize(L.TransactionDetails.type)),
     (details.status.verboseValue, localize(L.TransactionDetails.status)),
     (details.amount.coinFormatted, localize(L.TransactionDetails.amount)),
     (details.fee.coinFormatted, localize(L.TransactionDetails.fee)),
     (details.dateString, localize(L.TransactionDetails.date)),
     (details.fromAddress, localize(L.TransactionDetails.fromAddress)),
     (details.toAddress, localize(L.TransactionDetails.toAddress))].forEach {
      guard let text = $0 else { return }
      
      let view = TransactionDetailsRowView()
      view.configure(for: .text(text), with: $1)
      stackView.addArrangedSubview(view)
    }
  }
}

extension Reactive where Base == TransactionDetailsGeneralSectionView {
  var linkTap: Driver<Void> {
    return base.txidRowView.rx.tap
  }
}

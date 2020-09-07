import UIKit
import RxCocoa
import RxSwift
import SnapKit
import GiphyUISDK
import GiphyCoreSDK
import MaterialComponents

final class TransactionDetailsViewController: ModuleViewController<TransactionDetailsPresenter> {
  
  let rootScrollView = RootScrollView()
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 20
    return stackView
  }()
  
  let headerViewContainer = UIView()
  
  let headerView = HeaderView()
  
  override var shouldShowNavigationBar: Bool { return true }

  override func setupUI() {
    title = localize(L.TransactionDetails.title)
    
    view.addSubview(rootScrollView)
    rootScrollView.contentView.addSubviews(stackView)
    stackView.addArrangedSubview(headerViewContainer)
    headerViewContainer.addSubview(headerView)
  }

  override func setupLayout() {
    rootScrollView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide)
      $0.left.right.bottom.equalToSuperview()
    }
    rootScrollView.contentView.snp.makeConstraints {
      $0.height.greaterThanOrEqualToSuperview()
    }
    stackView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(30)
      $0.left.right.equalToSuperview()
      $0.bottom.lessThanOrEqualToSuperview().offset(-30)
    }
    headerView.snp.makeConstraints {
      $0.top.bottom.equalToSuperview()
      $0.left.equalToSuperview().offset(15)
      $0.right.lessThanOrEqualToSuperview().offset(-15)
    }
  }
  
  func setupUIBindings() {
    setupGeneralSection()
    setupGiftSection()
    setupExchangeSection()
    setupSellSection()
  }
  
  private func setupGeneralSection() {
    let details = presenter.details!
    let coinType = presenter.type!
    
    (details.txId ?? details.txDbId).flatMap { id in
      if let link = details.link, let linkURL = URL(string: link) {
        let linkView = LinkView()
        linkView.configure(text: id, link: linkURL)
        
        headerView.add(title: localize(L.TransactionDetails.Header.ID.title), valueView: linkView)
      } else {
        headerView.add(title: localize(L.TransactionDetails.Header.ID.title), value: id)
      }
    }
    
    headerView.add(title: localize(L.TransactionDetails.Header.TxType.title), value: details.type.verboseValue)
    
    let statusView = StatusView()
    statusView.configure(text: details.status.verboseValue, color: details.status.associatedColor)
    headerView.add(title: localize(L.TransactionDetails.Header.Status.title), valueView: statusView)
    
    if let cashStatus = details.cashStatus {
      let cashStatusView = StatusView()
      cashStatusView.configure(text: cashStatus.verboseValue, color: cashStatus.associatedColor)
      headerView.add(title: localize(L.TransactionDetails.Header.CashStatus.title), valueView: cashStatusView)
    }
    
    if let cryptoAmount = details.cryptoAmount, let fiatAmount = details.fiatAmount {
      let amountView = CryptoFiatAmountView()
      amountView.configure(cryptoAmount: cryptoAmount, fiatAmount: fiatAmount, type: coinType)
      headerView.add(title: localize(L.TransactionDetails.Header.Amount.title), valueView: amountView)
    }
    
    switch (details.cryptoFee, details.fiatFee) {
    case (.some(let cryptoFee), .none):
      let coinType = coinType == .catm ? CustomCoinType.ethereum : coinType
      let value = cryptoFee.coinFormatted.withCoinType(coinType)
      
      headerView.add(title: localize(L.TransactionDetails.Header.Fee.title), value: value)
    case (.none, .some(let fiatFee)):
      let value = fiatFee.fiatFormatted.withDollarSign
      
      headerView.add(title: localize(L.TransactionDetails.Header.Fee.title), value: value)
    case (.some(let cryptoFee), .some(let fiatFee)):
      let amountView = CryptoFiatAmountView()
      let coinType = coinType == .catm ? CustomCoinType.ethereum : coinType
      
      amountView.configure(cryptoAmount: cryptoFee, fiatAmount: fiatFee, type: coinType)
      headerView.add(title: localize(L.TransactionDetails.Header.Fee.title), valueView: amountView)
    default:
      break
    }
    
    if let date = details.dateString {
      headerView.add(title: localize(L.TransactionDetails.Header.Date.title), value: date)
    }
    
    if let fromAddress = details.fromAddress {
      headerView.add(title: localize(L.TransactionDetails.Header.FromAddress.title), value: fromAddress)
    }
    
    if let toAddress = details.toAddress {
      headerView.add(title: localize(L.TransactionDetails.Header.ToAddress.title), value: toAddress)
    }
  }
  
  private func setupGiftSection() {
    let details = presenter.details!
    
    if let phone = details.phone {
      headerView.add(title: localize(L.TransactionDetails.Header.Phone.title), value: phone)
    }
    
    if let imageId = details.imageId {
      let mediaView = GPHMediaView()
      
      GiphyCore.shared.gifByID(imageId) { (response, error) in
        guard let media = response?.data else { return }
        
        DispatchQueue.main.async { [weak self] in
          guard let _ = self else { return }
          
          mediaView.snp.makeConstraints {
            $0.width.equalTo(mediaView.snp.height).multipliedBy(media.aspectRatio)
          }
          
          mediaView.setMedia(media, rendition: .fixedHeightSmall)
        }
      }
      
      headerView.add(title: localize(L.TransactionDetails.Header.Image.title), valueView: mediaView)
    }
    
    if let message = details.message, message.count > 0 {
      headerView.add(title: localize(L.TransactionDetails.Header.Message.title), value: message)
    }
  }
  
  private func setupExchangeSection() {
    let details = presenter.details!
    
    details.refTxId.flatMap { id in
      if let link = details.refLink, let linkURL = URL(string: link) {
        let linkView = LinkView()
        linkView.configure(text: id, link: linkURL)
        
        headerView.add(title: localize(L.TransactionDetails.Header.RefID.title), valueView: linkView)
      } else {
        headerView.add(title: localize(L.TransactionDetails.Header.RefID.title), value: id)
      }
    }
    
    if let refCoin = details.refCoin, let refCryptoAmount = details.refCryptoAmount {
      headerView.add(title: localize(L.TransactionDetails.Header.RefCoin.title), value: refCoin.code)
      
      let amount = refCryptoAmount.coinFormatted.withCoinType(refCoin)
      headerView.add(title: localize(L.TransactionDetails.Header.RefAmount.title), value: amount)
    }
  }
  
  private func setupSellSection() {
    let details = presenter.details!
    
    if let sellInfo = details.sellInfo {
      headerView.add(title: localize(L.TransactionDetails.Header.SellQRCode.title), value: "")
      
      let qrCodeImageView = UIImageView(image: UIImage.qrCode(from: sellInfo))
      let qrCodeContainer = UIView()
      
      qrCodeContainer.addSubview(qrCodeImageView)
      
      qrCodeImageView.snp.makeConstraints {
        $0.top.bottom.equalToSuperview()
        $0.width.equalToSuperview().multipliedBy(0.5)
        $0.height.equalTo(qrCodeImageView.snp.width)
        $0.centerX.equalToSuperview()
      }
      
      stackView.addArrangedSubview(qrCodeContainer)
    }
  }

  override func setupBindings() {
    setupUIBindings()
    
    presenter.bind(input: TransactionDetailsPresenter.Input())
  }
  
  
}

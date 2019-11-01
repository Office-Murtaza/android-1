import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class CoinSellDetailsAnotherAddressViewController: NavigationScreenViewController<CoinSellDetailsAnotherAddressPresenter> {
  
  let qrCodeImageView = UIImageView(image: nil)
  
  let addressLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinDetails.address)
    label.textColor = .warmGrey
    label.font = .poppinsMedium14
    return label
  }()
  
  let addressValueLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .poppinsSemibold14
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.4
    return label
  }()
  
  let copyLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .copy)
    return label
  }()
  
  let amountLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .poppinsBold16
    return label
  }()
  
  let instructionsView: CoinSellDetailsInstructionsView = {
    let view = CoinSellDetailsInstructionsView()
    view.configure(instructions: [localize(L.CoinSellDetails.AnotherAddress.firstInstruction),
                                  localize(L.CoinSellDetails.AnotherAddress.secondInstruction),
                                  localize(L.CoinSellDetails.AnotherAddress.thirdInstruction),
                                  localize(L.CoinSellDetails.AnotherAddress.fourthInstruction)])
    return view
  }()
  
  let doneButton: MainButton = {
    let button = MainButton()
    button.configure(for: .lightDone)
    return button
  }()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    customView.contentView.addSubviews(qrCodeImageView,
                                       addressLabel,
                                       addressValueLabel,
                                       copyLabel,
                                       amountLabel,
                                       instructionsView,
                                       doneButton)
    customView.setTitle(localize(L.CoinSellDetails.title))
    
    qrCodeImageView.image = UIImage.qrCode(from: presenter.details.address)
    addressValueLabel.text = presenter.details.address
    amountLabel.text = presenter.amountString
  }

  override func setupLayout() {
    qrCodeImageView.snp.makeConstraints {
      $0.top.equalTo(customView.backgroundImageView.snp.bottom).offset(35)
      $0.centerX.equalToSuperview()
    }
    addressLabel.snp.makeConstraints {
      $0.top.equalTo(qrCodeImageView.snp.bottom).offset(25)
      $0.centerX.equalToSuperview()
    }
    addressValueLabel.snp.makeConstraints {
      $0.top.equalTo(addressLabel.snp.bottom).offset(10)
      $0.centerX.equalToSuperview()
      $0.left.greaterThanOrEqualToSuperview().offset(25)
      $0.right.lessThanOrEqualToSuperview().offset(-25)
    }
    copyLabel.snp.makeConstraints {
      $0.top.equalTo(addressValueLabel.snp.bottom).offset(10)
      $0.centerX.equalToSuperview()
    }
    amountLabel.snp.makeConstraints {
      $0.top.equalTo(copyLabel.snp.bottom).offset(30)
      $0.centerX.equalToSuperview()
    }
    instructionsView.snp.makeConstraints {
      $0.top.equalTo(amountLabel.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(25)
    }
    doneButton.snp.makeConstraints {
      $0.top.equalTo(instructionsView.snp.bottom).offset(25)
      $0.centerX.equalToSuperview()
      $0.width.equalTo(150)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  func setupUIBindings() {
    
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let copyDriver = copyLabel.rx.tap
    let doneDriver = doneButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinSellDetailsAnotherAddressPresenter.Input(back: backDriver,
                                                                       copy: copyDriver,
                                                                       done: doneDriver))
  }
  
  
}

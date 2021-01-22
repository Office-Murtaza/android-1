import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinSellDetailsAnotherAddressViewController: NavigationScreenViewController<CoinSellDetailsAnotherAddressPresenter> {
  
  let qrCodeCardView = QRCodeCardView()
  
  let amountLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .systemFont(ofSize: 20, weight: .medium)
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
  
  let doneButton = MDCButton.done
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override var shouldShowNavigationBar: Bool { return false }

  override func setupUI() {
    customView.contentView.addSubviews(qrCodeCardView,
                                       amountLabel,
                                       instructionsView,
                                       doneButton)
    customView.setTitle(presenter.title)
  }

  override func setupLayout() {
    qrCodeCardView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview().inset(30)
    }
    amountLabel.snp.makeConstraints {
      $0.top.equalTo(qrCodeCardView.snp.bottom).offset(40)
      $0.centerX.equalToSuperview()
    }
    instructionsView.snp.makeConstraints {
      $0.top.equalTo(amountLabel.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(15)
    }
    doneButton.snp.makeConstraints {
      $0.top.equalTo(instructionsView.snp.bottom).offset(30)
      $0.centerX.equalToSuperview()
      $0.width.equalTo(150)
      $0.height.equalTo(48)
      $0.bottom.equalToSuperview().inset(40)
    }
  }
  
  func setupUIBindings() {
    qrCodeCardView.configure(for: presenter.details.address)
    amountLabel.text = presenter.amountString
    
    qrCodeCardView.rx.copy
      .drive(onNext: { [unowned self] in self.view.makeToast(localize(L.Shared.copied)) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let copyDriver = qrCodeCardView.rx.copy
    let doneDriver = doneButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinSellDetailsAnotherAddressPresenter.Input(back: backDriver,
                                                                       copy: copyDriver,
                                                                       done: doneDriver))
  }
}

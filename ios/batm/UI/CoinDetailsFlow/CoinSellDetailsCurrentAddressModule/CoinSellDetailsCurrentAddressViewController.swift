import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class CoinSellDetailsCurrentAddressViewController: NavigationScreenViewController<CoinSellDetailsCurrentAddressPresenter> {
  
  let instructionsView: CoinSellDetailsInstructionsView = {
    let view = CoinSellDetailsInstructionsView()
    view.configure(instructions: [localize(L.CoinSellDetails.CurrentAddress.firstInstruction),
                                  localize(L.CoinSellDetails.CurrentAddress.secondInstruction),
                                  localize(L.CoinSellDetails.CurrentAddress.thirdInstruction),
                                  localize(L.CoinSellDetails.CurrentAddress.fourthInstruction)])
    return view
  }()
  
  let doneButton: MainButton = {
    let button = MainButton()
    button.configure(for: .done)
    return button
  }()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    customView.contentView.addSubviews(instructionsView,
                                       doneButton)
    customView.setTitle(presenter.title)
  }

  override func setupLayout() {
    instructionsView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(40)
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
    let doneDriver = doneButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinSellDetailsCurrentAddressPresenter.Input(back: backDriver,
                                                                       done: doneDriver))
  }
  
  
}

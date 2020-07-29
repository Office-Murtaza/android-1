import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents
import MessageUI

class WelcomeViewController: ModuleViewController<WelcomePresenter>, MFMailComposeViewControllerDelegate {
  
  let sliderView = WelcomeSliderView()
  let buttonsView = WelcomeButtonsView()
  let contactSupportButton = MDCButton.contactSupport
  
  override func setupUI() {
    view.backgroundColor = .white
    
    view.addSubviews(sliderView,
                     buttonsView,
                     contactSupportButton)
  }
  
  override func setupLayout() {
    sliderView.snp.makeConstraints {
      $0.left.right.equalToSuperview()
      $0.bottom.equalTo(buttonsView.snp.top)
      $0.top.greaterThanOrEqualToSuperview().offset(50).priority(.required)
    }
    buttonsView.snp.makeConstraints {
      $0.bottom.equalTo(contactSupportButton.snp.top).offset(-50)
      $0.left.right.equalToSuperview().inset(15)
    }
    contactSupportButton.snp.makeConstraints {
      $0.bottom.equalTo(view.safeAreaLayoutGuide).offset(-10)
      $0.centerX.equalToSuperview()
    }
  }
  
  private func setupUIBindings() {
    
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let createDriver = buttonsView.rx.createTap
    let recoverDriver = buttonsView.rx.recoverTap
    let contactSupportDriver = contactSupportButton.rx.tap.asDriver()
    presenter.bind(input: WelcomePresenter.Input(create: createDriver,
                                                 recover: recoverDriver,
                                                 contactSupport: contactSupportDriver))
  }
  
  func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
    dismiss(animated: true)
  }
}

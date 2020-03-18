import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class VerificationInfoViewController: NavigationScreenViewController<VerificationInfoPresenter> {
  
  let containerView = UIView()
  let statusRowView = VerificationInfoRowView()
  let firstDivider = Divider()
  let txLimitRowView = VerificationInfoRowView()
  let dailyLimitRowView = VerificationInfoRowView()
  let lastDivider = Divider()
  
  let messageLabel: UILabel = {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = .poppinsMedium14
    label.numberOfLines = 0
    return label
  }()
  
  let verifyButton: MainButton = {
    let button = MainButton()
    button.isHidden = true
    return button
  }()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    customView.setTitle(localize(L.VerificationInfo.title))
    customView.contentView.addSubviews(containerView)
    
    containerView.addSubviews(statusRowView,
                              firstDivider,
                              txLimitRowView,
                              dailyLimitRowView,
                              lastDivider,
                              messageLabel,
                              verifyButton)
  }

  override func setupLayout() {
    containerView.snp.makeConstraints {
      $0.top.bottom.equalToSuperview().inset(35)
      $0.left.right.equalToSuperview().inset(30)
    }
    statusRowView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    firstDivider.snp.makeConstraints {
      $0.top.equalTo(statusRowView.snp.bottom).offset(15)
      $0.left.right.equalToSuperview()
    }
    txLimitRowView.snp.makeConstraints {
      $0.top.equalTo(firstDivider.snp.bottom).offset(15)
      $0.left.right.equalToSuperview()
    }
    dailyLimitRowView.snp.makeConstraints {
      $0.top.equalTo(txLimitRowView.snp.bottom).offset(20)
      $0.left.right.equalToSuperview()
    }
    lastDivider.snp.makeConstraints {
      $0.top.equalTo(dailyLimitRowView.snp.bottom).offset(15)
      $0.left.right.equalToSuperview()
    }
    messageLabel.snp.makeConstraints {
      $0.top.equalTo(lastDivider.snp.bottom).offset(15)
      $0.left.right.equalToSuperview()
    }
    verifyButton.snp.makeConstraints {
      $0.top.equalTo(messageLabel.snp.bottom).offset(35)
      $0.centerX.bottom.equalToSuperview()
      $0.width.equalTo(160)
    }
  }
  
  private func update(with info: VerificationInfo) {
    statusRowView.configure(for: .status(info.status))
    txLimitRowView.configure(for: .txLimit(info.txLimit))
    dailyLimitRowView.configure(for: .dailyLimit(info.dailyLimit))
    messageLabel.text = info.message
    verifyButton.isHidden = !info.status.needAnyVerification
    
    if info.status.needVerification {
      verifyButton.configure(for: .verify)
    } else if info.status.needVIPVerification {
      verifyButton.configure(for: .vipVerify)
    }
  }
  
  func setupUIBindings() {
    presenter.infoRelay
      .observeOn(MainScheduler.instance)
      .subscribe(onNext: { [unowned self] in self.update(with: $0) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let verifyDriver = verifyButton.rx.tap.asDriver()
    
    presenter.bind(input: VerificationInfoPresenter.Input(back: backDriver,
                                                          verify: verifyDriver))
  }
}

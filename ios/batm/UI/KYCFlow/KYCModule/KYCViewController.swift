import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class KYCViewController: ModuleViewController<KYCPresenter> {
  
  let rootScrollView = RootScrollView()
  
  let headerView = KYCHeaderView()
  
  let infoView: InfoView = {
    let view = InfoView()
    view.isHidden = true
    return view
  }()
  
  let verifyButton = MDCButton.verify
  
  override var shouldShowNavigationBar: Bool { return true }

  override func setupUI() {
    title = localize(L.KYC.title)
    
    view.addSubviews(rootScrollView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(headerView,
                                           infoView,
                                           verifyButton)
  }

  override func setupLayout() {
    rootScrollView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide)
      $0.left.right.bottom.equalToSuperview()
    }
    rootScrollView.contentView.snp.makeConstraints {
      $0.height.greaterThanOrEqualToSuperview()
    }
    headerView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(30)
      $0.left.equalToSuperview().offset(15)
      $0.right.lessThanOrEqualToSuperview().offset(-15)
    }
    infoView.snp.makeConstraints {
      $0.top.equalTo(headerView.snp.bottom).offset(25)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.lessThanOrEqualTo(verifyButton.snp.top).offset(-25)
    }
    verifyButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  private func update(with kyc: KYC) {
    headerView.configure(for: kyc)
    infoView.isHidden = kyc.message == nil
    infoView.setup(with: kyc.message ?? "")
    verifyButton.isHidden = !kyc.status.needAnyVerification
    
    if kyc.status.needVerification {
      verifyButton.setTitle(localize(L.KYC.Button.verify), for: .normal)
    } else if kyc.status.needVIPVerification {
      verifyButton.setTitle(localize(L.KYC.Button.vipVerify), for: .normal)
    }
  }
  
  func setupUIBindings() {
    presenter.kycRelay
      .observeOn(MainScheduler.instance)
      .subscribe(onNext: { [unowned self] in self.update(with: $0) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let verifyDriver = verifyButton.rx.tap.asDriver()
    
    presenter.bind(input: KYCPresenter.Input(verify: verifyDriver))
  }
}

import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents

class UnlinkViewController: ModuleViewController<UnlinkPresenter> {
  
  let rootScrollView = RootScrollView()
    
  let infoView: InfoView = {
    let view = InfoView()
    view.setup(with: localize(L.Unlink.annotation))
    return view
  }()
  
  let unlinkButton = MDCButton.unlink
  
  override func setupUI() {
    title = localize(L.Unlink.title)
    
    view.addSubviews(rootScrollView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(infoView,
                                           unlinkButton)
  }
  
  override func setupLayout() {
    rootScrollView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide)
      $0.left.right.bottom.equalToSuperview()
    }
    rootScrollView.contentView.snp.makeConstraints {
      $0.height.equalToSuperview()
    }
    
    infoView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(40)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.lessThanOrEqualTo(unlinkButton.snp.top).offset(-40)
    }
    unlinkButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  private func setupUIBindings() {
    unlinkButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let unlinkDriver = unlinkButton.rx.tap.asDriver()
    
    presenter.bind(input: UnlinkPresenter.Input(unlink: unlinkDriver))
  }
}

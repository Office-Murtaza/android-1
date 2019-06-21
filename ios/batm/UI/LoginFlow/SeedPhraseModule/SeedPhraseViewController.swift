import UIKit
import RxSwift
import RxCocoa
import SnapKit

class SeedPhraseViewController: ModuleViewController<SeedPhrasePresenter> {
  
  let backgroundImageView: UIImageView = {
    let imageView = UIImageView(image: UIImage(named: "login_background"))
    imageView.contentMode = .scaleAspectFill
    imageView.clipsToBounds = true
    return imageView
  }()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.SeedPhrase.title)
    label.textColor = .white
    label.font = .poppinsSemibold22
    return label
  }()
  
  let separatorView = GoldSeparatorView()
  
  let mainView = SeedPhraseView()
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    view.backgroundColor = .whiteThree
    
    view.addSubviews(backgroundImageView,
                     titleLabel,
                     separatorView,
                     mainView)
  }
  
  override func setupLayout() {
    backgroundImageView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(187)
    }
    titleLabel.snp.makeConstraints {
      $0.top.equalToSuperview().offset(63)
      $0.centerX.equalToSuperview()
    }
    separatorView.snp.makeConstraints {
      $0.top.equalTo(titleLabel.snp.bottom).offset(15)
      $0.centerX.equalToSuperview()
    }
    mainView.snp.makeConstraints {
      $0.top.equalTo(separatorView.snp.bottom).offset(15)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-30)
    }
  }
  
  private func setupUIBindings() {
    presenter.seedPhraseRelay
      .observeOn(MainScheduler.instance)
      .filterNil()
      .map { $0.split(separator: " ").map { String($0) } }
      .subscribe(onNext: { [mainView] in mainView.configure(for: $0) })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let copyDriver = mainView.rx.copyTap
    let doneDriver = mainView.rx.doneTap
    presenter.bind(input: SeedPhrasePresenter.Input(copy: copyDriver,
                                                    done: doneDriver))
  }
}

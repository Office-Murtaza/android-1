import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class ShowSeedPhraseViewController: ModuleViewController<ShowSeedPhrasePresenter> {
  
  let backgroundImageView: UIImageView = {
    let imageView = UIImageView(image: UIImage(named: "login_background"))
    imageView.contentMode = .scaleAspectFill
    imageView.clipsToBounds = true
    return imageView
  }()
  
  let safeAreaContainer = UIView()
  
  let backButton: UIButton = {
    let button = UIButton()
    button.setImage(UIImage(named: "back"), for: .normal)
    return button
  }()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.ShowSeedPhrase.title)
    label.textColor = .white
    label.font = .poppinsSemibold20
    return label
  }()
  
  let mainView = SeedPhraseView(flat: true)
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    view.backgroundColor = .white
    
    view.addSubviews(mainView,
                     backgroundImageView,
                     safeAreaContainer)
    safeAreaContainer.addSubviews(backButton,
                                  titleLabel)
  }

  override func setupLayout() {
    backgroundImageView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.bottom.equalTo(view.safeAreaLayoutGuide.snp.top).offset(44)
    }
    safeAreaContainer.snp.makeConstraints {
      $0.left.right.bottom.equalTo(backgroundImageView)
      $0.top.equalTo(view.safeAreaLayoutGuide)
    }
    backButton.snp.makeConstraints {
      $0.centerY.equalTo(titleLabel)
      $0.left.equalToSuperview().offset(15)
      $0.size.equalTo(45)
    }
    titleLabel.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    mainView.snp.makeConstraints {
      $0.top.equalTo(backgroundImageView.snp.bottom).offset(-15)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalTo(view.safeAreaLayoutGuide).offset(-30)
    }
  }
  
  func setupUIBindings() {
    presenter.seedPhraseRelay
      .observeOn(MainScheduler.instance)
      .filterNil()
      .map { $0.split(separator: " ").map { String($0) } }
      .subscribe(onNext: { [mainView] in mainView.configure(for: $0) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = backButton.rx.tap.asDriver()
    let copyDriver = mainView.rx.copyTap
    let doneDriver = mainView.rx.doneTap
    
    presenter.bind(input: ShowSeedPhrasePresenter.Input(back: backDriver,
                                                        copy: copyDriver,
                                                        done: doneDriver))
  }
}

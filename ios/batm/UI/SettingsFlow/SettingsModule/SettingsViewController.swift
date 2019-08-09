import UIKit
import RxSwift
import RxCocoa
import SnapKit

class SettingsViewController: ModuleViewController<SettingsPresenter>, UICollectionViewDelegateFlowLayout {
  
  var dataSource: SettingsCollectionViewDataSource!
  
  let backgroundImageView: UIImageView = {
    let imageView = UIImageView(image: UIImage(named: "login_background"))
    imageView.contentMode = .scaleAspectFill
    imageView.clipsToBounds = true
    return imageView
  }()
  
  let safeAreaContainer = UIView()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.Settings.title)
    label.textColor = .white
    label.font = .poppinsSemibold20
    return label
  }()
  
  let collectionView: UICollectionView = {
    let layout = UICollectionViewFlowLayout()
    let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
    return collectionView
  }()
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    view.backgroundColor = .white
    
    view.addSubviews(backgroundImageView,
                     safeAreaContainer,
                     collectionView)
    safeAreaContainer.addSubview(titleLabel)
    
    collectionView.backgroundColor = .clear
    collectionView.delegate = self
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
    titleLabel.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    collectionView.snp.makeConstraints {
      $0.top.equalTo(backgroundImageView.snp.bottom)
      $0.left.right.bottom.equalToSuperview()
    }
  }
  
  private func setupUIBindings() {
    collectionView.dataSource = dataSource
    dataSource.collectionView = collectionView
    
    presenter.typesRelay
      .observeOn(MainScheduler.instance)
      .bind(to: dataSource.typesRelay)
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let selectDriver = collectionView.rx.itemSelected.asDriver()
    
    presenter.bind(input: SettingsPresenter.Input(select: selectDriver))
  }
  
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath) -> CGSize {
    return CGSize(width: collectionView.bounds.width - 50, height: 55)
  }
  
}

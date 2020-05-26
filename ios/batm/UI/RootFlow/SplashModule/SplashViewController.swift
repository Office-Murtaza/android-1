import UIKit
import RxCocoa
import RxSwift

protocol SplashModule: class { }

final class SplashViewController: UIViewController, SplashModule {

  let logoImageView = UIImageView(image: UIImage(named: "splash_logo"))

  // MARK: Life cycle
  
  override func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
    setupLayout()
  }

  func setupUI() {
    view.backgroundColor = .white
    view.addSubview(logoImageView)
  }
  
  func setupLayout() {
    logoImageView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
  }
}
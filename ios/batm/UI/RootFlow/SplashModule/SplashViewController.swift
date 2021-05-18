import UIKit
import RxCocoa
import RxSwift

protocol SplashModule: AnyObject { }

final class SplashViewController: UIViewController, SplashModule {

  let logoImageView = UIImageView(image: UIImage(named: "logo"))

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

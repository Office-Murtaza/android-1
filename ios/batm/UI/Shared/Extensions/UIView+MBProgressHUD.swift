import UIKit
import RxSwift
import RxCocoa
import MBProgressHUD

extension Reactive where Base: UIView {
  
  var showHUD: Binder<Bool> {
    return Binder(base) { target, value in
      if value {
        MBProgressHUD.showAdded(to: target, animated: true)
      } else {
        MBProgressHUD.hide(for: target, animated: true)
      }
    }
  }
  
  var showHUDWithProgress: Binder<Double?> {
    return Binder(base) { target, value in
      guard let progressValue = value else {
        MBProgressHUD(for: target)?.hide(animated: true)
        return
      }
      self.presentedProgressHUD(for: target).progress = Float(progressValue)
    }
  }
  
  var hudCancelDriver: Driver<Void>? {
    let hud = MBProgressHUD(for: base)
    hud?.button.setTitle(localize(L.Shared.cancel), for: .normal)
    return hud?.button.rx.tap.asDriver()
  }
  
  private func presentedProgressHUD(for view: UIView) -> MBProgressHUD {
    if let hud = MBProgressHUD(for: view) {
      return hud
    }
    let hud = MBProgressHUD.showAdded(to: view, animated: true)
    hud.mode = .determinate
    return hud
  }
}

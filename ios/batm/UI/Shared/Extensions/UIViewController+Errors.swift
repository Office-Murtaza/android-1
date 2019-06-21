import UIKit
import RxCocoa
import RxSwift

extension Reactive where Base: UIViewController {
  var errors: Binder<Error> {
    return Binder(base) { controller, error in
      let message: String
      
      if let apiError = error as? APIError, case .serverError = apiError {
        return
      }
      
      var errorMessage = localize(L.Shared.Error.message)
      #if DEBUG
      errorMessage += "\n" + String(describing: error)
      #endif
      message = errorMessage
      
      let alert = UIAlertController(title: localize(L.Shared.Error.title),
                                    message: message,
                                    preferredStyle: .alert)
      let okAction = UIAlertAction(title: localize(L.Shared.ok), style: .default)
      alert.addAction(okAction)
      
      controller.present(alert, animated: true, completion: nil)
    }
  }
}

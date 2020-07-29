import UIKit
import RxCocoa
import RxSwift

extension Reactive where Base: UIViewController {
  var errors: Binder<Error> {
    return Binder(base) { controller, error in
      if error is PinCodeError {
        return
      }
      
      if let apiError = error as? APIError, case let .serverError(serverError) = apiError, serverError.code != 1 {
        return
      }
      
      var errorMessage: String
      
      if let apiError = error as? APIError, case .noConnection = apiError {
        errorMessage = localize(L.Shared.Error.NoConnection.message)
      } else {
        errorMessage = localize(L.Shared.Error.message)
        #if DEBUG
        errorMessage += "\n" + String(describing: error)
        #endif
      }
      
      let alert = UIAlertController(title: localize(L.Shared.Error.title),
                                    message: errorMessage,
                                    preferredStyle: .alert)
      let okAction = UIAlertAction(title: localize(L.Shared.ok), style: .default)
      alert.addAction(okAction)
      
      controller.present(alert, animated: true, completion: nil)
    }
  }
}

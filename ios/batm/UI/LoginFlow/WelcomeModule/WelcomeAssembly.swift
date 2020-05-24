import Foundation
import Swinject

class WelcomeAssembly: Assembly {
  
  func assembleActionSheet(container: Container) {
    container.register(UIAlertController.self) { (ioc, controller: UIViewController) in
      let phone = localize(L.Welcome.Support.phone)
      let email = localize(L.Welcome.Support.mail)
      let title = localize(L.Welcome.Support.title)
      let message = String(format: localize(L.Welcome.Support.message), phone, email)
      
      let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
      
      let showToast = {
        controller.view.makeToast(localize(L.Shared.copied))
      }
      
      alert.addAction(UIAlertAction(title: localize(L.Welcome.Support.copyPhone), style: .default) { _ in
        UIPasteboard.general.string = phone
        showToast()
      })
      
      alert.addAction(UIAlertAction(title: localize(L.Welcome.Support.copyMail), style: .default) { _ in
        UIPasteboard.general.string = email
        showToast()
      })
      
      alert.addAction(UIAlertAction(title: localize(L.Shared.cancel), style: .cancel))
      
      return alert
    }
  }
  
  func assemble(container: Container) {
    assembleActionSheet(container: container)
    
    container.register(Module<WelcomeModule>.self) { resolver in
      let viewController = WelcomeViewController()
      let presenter = WelcomePresenter()
      
      presenter.delegate = resolver.resolve(WelcomeModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<WelcomeModule>(controller: viewController, input: presenter)
    }
  }
}

import Foundation
import Swinject
import MessageUI

class WelcomeAssembly: Assembly {
  
  func assembleActionSheet(container: Container) {
    container.register(UIAlertController.self) { (ioc, controller: UIViewController) in
      let phone = localize(L.Welcome.Support.phone)
      let email = localize(L.Welcome.Support.mail)
      let message = String(format: localize(L.Welcome.Support.message), phone, email)
      
      let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
      
      alert.addAction(UIAlertAction(title: localize(L.Welcome.Support.call), style: .default) { _ in
        let cleanPhone = "+" + phone.components(separatedBy: CharacterSet.decimalDigits.inverted).joined()
        
        if let url = URL(string: "tel://\(cleanPhone)"), UIApplication.shared.canOpenURL(url) {
          UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }
      })
      
      alert.addAction(UIAlertAction(title: localize(L.Welcome.Support.send), style: .default) { _ in
        if MFMailComposeViewController.canSendMail(), let delegate = controller as? MFMailComposeViewControllerDelegate {
            let mail = MFMailComposeViewController()
            mail.mailComposeDelegate = delegate
            mail.setToRecipients([email])

            controller.present(mail, animated: true)
        }
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

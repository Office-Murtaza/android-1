import UIKit
import MaterialComponents

extension MDCButton {
  
  static let defaultContentEdgeInsets = UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8)
  
  static var `default`: MDCButton {
    let button = MDCButton()
    button.contentEdgeInsets = defaultContentEdgeInsets
    return button
  }
  
  static var text: MDCButton {
    let button = MDCButton.default
    button.applyTextTheme(withScheme: MDCContainerScheme.default)
    return button
  }
  
  static var outlined: MDCButton {
    let button = MDCButton.default
    button.applyOutlinedTheme(withScheme: MDCContainerScheme.default)
    return button
  }
  
  static var contained: MDCButton {
    let button = MDCButton.default
    button.applyContainedTheme(withScheme: MDCContainerScheme.default)
    return button
  }
  
  static var max: MDCButton {
    let button = MDCButton.text
    button.setTitle(localize(L.Shared.Button.max), for: .normal)
    
    button.snp.makeConstraints {
      $0.width.equalTo(48)
    }
    
    return button
  }
  
  static var paste: MDCButton {
    let button = MDCButton.text
    button.setTitle(localize(L.Shared.Button.paste), for: .normal)
    return button
  }
  
  static var scan: MDCButton {
    let button = MDCButton.text
    button.setImage(UIImage(named: "scan"), for: .normal)
    return button
  }
  
  static var add: MDCButton {
    let button = MDCButton.text
    button.contentEdgeInsets = .zero
    button.setTitle(localize(L.Shared.Button.add), for: .normal)
    return button
  }
  
  static var remove: MDCButton {
    let button = MDCButton.text
    button.contentEdgeInsets = .zero
    button.setTitle(localize(L.Shared.Button.remove), for: .normal)
    return button
  }
  
  static var next: MDCButton {
    let button = MDCButton.contained
    button.setTitle(localize(L.Shared.Button.next), for: .normal)
    return button
  }
  
  static var done: MDCButton {
    let button = MDCButton.contained
    button.setTitle(localize(L.Shared.Button.done), for: .normal)
    return button
  }
  
  static var copy: MDCButton {
    let button = MDCButton.text
    button.setTitle(localize(L.Shared.Button.copy), for: .normal)
    return button
  }
  
  static var createNewWallet: MDCButton {
    let button = MDCButton.contained
    button.setTitle(localize(L.Welcome.CreateButton.title), for: .normal)
    return button
  }
  
  static var recoverMyWallet: MDCButton {
    let button = MDCButton.outlined
    button.setTitle(localize(L.Welcome.RecoverButton.title), for: .normal)
    return button
  }
  
  static var contactSupport: MDCButton {
    let button = MDCButton.text
    button.setTitle(localize(L.Welcome.contactSupport), for: .normal)
    return button
  }
  
  func set(image: UIImage?, title: String?, spacing: CGFloat = 10) {
    setTitle(title, for: .normal)
    setImage(image, for: .normal)
    imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: spacing)
    titleEdgeInsets = UIEdgeInsets(top: 0, left: spacing, bottom: 0, right: 0)
  }
  
}

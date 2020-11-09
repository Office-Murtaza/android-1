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
  
  static var secondaryText: MDCButton {
    let button = MDCButton.default
    button.applyTextTheme(withScheme: MDCContainerScheme.default)
    button.setBackgroundColor(MDCContainerScheme.default.colorScheme.primaryColor.withAlphaComponent(0.08))
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
  
  static var secondaryPaste: MDCButton {
    let button = MDCButton.secondaryText
    button.setTitle(localize(L.Shared.Button.paste), for: .normal)
    return button
  }
  
  static var scan: MDCButton {
    let button = MDCButton.text
    button.setImage(UIImage(named: "scan"), for: .normal)
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
  
  static var secondaryCopy: MDCButton {
     let button = MDCButton.secondaryText
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
  
  static var sendRequest: MDCButton {
    let button = MDCButton.contained
    button.setTitle(localize(L.BuySellTradeDetails.Button.sendRequest), for: .normal)
    return button
  }
  
  static var create: MDCButton {
    let button = MDCButton.contained
    button.setTitle(localize(L.Shared.Button.create), for: .normal)
    return button
  }
  
  static var reserve: MDCButton {
    let button = MDCButton.contained
    button.setTitle(localize(L.Reserve.Button.reserve), for: .normal)
    return button
  }
  
  static var recall: MDCButton {
    let button = MDCButton.contained
    button.setTitle(localize(L.Recall.Button.recall), for: .normal)
    return button
  }
  
  static var cancel: MDCButton {
    let button = MDCButton.secondaryText
    button.setTitle(localize(L.Shared.Button.cancel), for: .normal)
    return button
  }
  
  static var withdraw: MDCButton {
    let button = MDCButton.secondaryText
    button.setTitle(localize(L.CoinStaking.Button.withdraw), for: .normal)
    return button
  }
  
  static var update: MDCButton {
    let button = MDCButton.contained
    button.setTitle(localize(L.Shared.Button.update), for: .normal)
    return button
  }
  
  static var unlink: MDCButton {
    let button = MDCButton.contained
    button.setTitle(localize(L.Shared.Button.unlink), for: .normal)
    return button
  }
  
  static var verify: MDCButton {
    let button = MDCButton.contained
    button.setTitle(localize(L.KYC.Button.verify), for: .normal)
    return button
  }
  
  static var send: MDCButton {
    let button = MDCButton.contained
    button.setTitle(localize(L.Shared.Button.send), for: .normal)
    return button
  }
  
  static var plus: MDCButton {
    let button = MDCButton.contained
    button.setImage(UIImage(named: "colored_plus"), for: .normal)
    button.setBackgroundColor(.white)
    return button
  }
  
  static var close: MDCButton {
    let button = MDCButton.contained
    button.setImage(UIImage(named: "colored_close"), for: .normal)
    button.setBackgroundColor(.white)
    return button
  }
  
  static var submit: MDCButton {
    let button = MDCButton.contained
    button.setTitle(localize(L.Shared.Button.submit), for: .normal)
    return button
  }
  
  func set(image: UIImage?, title: String?, spacing: CGFloat = 10) {
    setTitle(title, for: .normal)
    setImage(image, for: .normal)
    imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: spacing)
    titleEdgeInsets = UIEdgeInsets(top: 0, left: spacing, bottom: 0, right: 0)
  }
  
}

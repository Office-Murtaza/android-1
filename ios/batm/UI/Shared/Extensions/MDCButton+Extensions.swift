import UIKit
import MaterialComponents

extension MDCButton {
  
  static let defaultEdgeInsets = UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8)
  
  static var `default`: MDCButton {
    let button = MDCButton()
    button.setTitleColor(.ceruleanBlue, for: .normal)
    button.contentEdgeInsets = defaultEdgeInsets
    button.setBackgroundColor(.white)
    return button
  }
  
  static var primary: MDCButton {
    let button = MDCButton()
    button.setTitleColor(.white, for: .normal)
    button.contentEdgeInsets = defaultEdgeInsets
    button.setBackgroundColor(.ceruleanBlue)
    button.layer.cornerRadius = 4
    return button
  }
  
  static var max: MDCButton {
    let button = MDCButton.default
    button.setTitle(localize(L.Shared.Button.max), for: .normal)
    return button
  }
  
  static var paste: MDCButton {
    let button = MDCButton.default
    button.setTitle(localize(L.Shared.Button.paste), for: .normal)
    return button
  }
  
  static var scan: MDCButton {
    let button = MDCButton.default
    button.setImage(UIImage(named: "scan"), for: .normal)
    return button
  }
  
  static var add: MDCButton {
    let button = MDCButton.default
    button.contentEdgeInsets = .zero
    button.setTitle(localize(L.Shared.Button.add), for: .normal)
    return button
  }
  
  static var remove: MDCButton {
    let button = MDCButton.default
    button.contentEdgeInsets = .zero
    button.setTitle(localize(L.Shared.Button.remove), for: .normal)
    return button
  }
  
  static var next: MDCButton {
    let button = MDCButton.primary
    button.setTitle(localize(L.CoinWithdraw.Button.next), for: .normal)
    return button
  }
  
}

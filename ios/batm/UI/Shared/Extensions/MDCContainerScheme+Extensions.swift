import UIKit
import MaterialComponents

extension MDCContainerScheme {
  
  static var `default`: MDCContainerScheme {
    let scheme = MDCContainerScheme()
    scheme.colorScheme = MDCSemanticColorScheme(defaults: .material201907)
    scheme.colorScheme.primaryColor = .ceruleanBlue
    scheme.colorScheme.onPrimaryColor = .white
    scheme.colorScheme.onSurfaceColor = .blackThree
    return scheme
  }
  
}

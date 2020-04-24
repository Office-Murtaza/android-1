import UIKit
import MaterialComponents

class ThemedTextInputControllerOutlined: MDCTextInputControllerOutlined {
  
  override init() {
    super.init()
  }
  
  required init(textInput input: (UIView & MDCTextInput)?) {
    super.init(textInput: input)
    
    self.applyTheme(withScheme: MDCContainerScheme.default)
  }
  
}

class ThemedTextInputControllerOutlinedTextArea: MDCTextInputControllerOutlinedTextArea {
  
  override init() {
    super.init()
  }
  
  required init(textInput input: (UIView & MDCTextInput)?) {
    super.init(textInput: input)
    
    self.activeColor = MDCContainerScheme.default.colorScheme.primaryColor
    self.normalColor = MDCContainerScheme.default.colorScheme.onSurfaceColor.withAlphaComponent(0.6)
    self.borderStrokeColor = MDCContainerScheme.default.colorScheme.onSurfaceColor.withAlphaComponent(0.6)
    self.inlinePlaceholderColor = MDCContainerScheme.default.colorScheme.onSurfaceColor.withAlphaComponent(0.6)
    self.floatingPlaceholderActiveColor = MDCContainerScheme.default.colorScheme.primaryColor.withAlphaComponent(0.89)
    self.floatingPlaceholderNormalColor = MDCContainerScheme.default.colorScheme.onSurfaceColor.withAlphaComponent(0.6)
    input?.textColor = MDCContainerScheme.default.colorScheme.onSurfaceColor
  }
  
}

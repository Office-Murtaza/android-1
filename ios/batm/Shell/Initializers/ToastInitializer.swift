import Foundation
import Swinject
import Toast_Swift

class ToastInitializer: Initializer {
  
  func initialize(with options: LaunchOptions?, assembler: Assembler, container: Container) {
    ToastManager.shared.duration = 1
  }
  
}

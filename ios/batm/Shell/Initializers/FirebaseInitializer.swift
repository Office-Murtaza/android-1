import Foundation
import Swinject
import FirebaseCore

class FirebaseInitializer: Initializer {
  
  func initialize(with options: LaunchOptions?, assembler: Assembler, container: Container) {
    FirebaseApp.configure()
  }
  
}

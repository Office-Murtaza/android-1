import Foundation
import Swinject
import Firebase

class FirebaseInitializer: Initializer {
  
  func initialize(with options: LaunchOptions?, assembler: Assembler, container: Container) {
    FirebaseApp.configure()
  }
  
}

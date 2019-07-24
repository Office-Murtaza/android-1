import Foundation
import Swinject
import GoogleMaps

class GoogleServicesInitializer: Initializer {
  
  func initialize(with options: LaunchOptions?, assembler: Assembler, container: Container) {
    GMSServices.provideAPIKey("AIzaSyDGBWQKBBKloFJfQgY_5i-l-a7aT8oRWhw")
  }
  
}

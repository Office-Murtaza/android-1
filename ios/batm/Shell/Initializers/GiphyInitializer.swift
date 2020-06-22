import Foundation
import Swinject
import GiphyUISDK

class GiphyInitializer: Initializer {
  
  func initialize(with options: LaunchOptions?, assembler: Assembler, container: Container) {
    Giphy.configure(apiKey: "8IEBjOFUS31WQY6zK2ryvf9xMCxSYTpM")
  }
  
}

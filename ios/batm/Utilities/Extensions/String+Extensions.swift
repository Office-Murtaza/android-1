import Foundation

extension String {
  
  func nilIfEmpty() -> String? {
    return self.isEmpty ? nil : self
  }
  
}

import Foundation

struct PickerConfig: Equatable {
  
  let title: String
  let cancelTitle: String
  let items: [PickerItem]
  
  struct PickerItem: Equatable {
    
    enum SourceType {
      case camera
      case library
    }
    
    let itemTitle: String
    let sourceType: SourceType
    let editable: Bool
  }
}

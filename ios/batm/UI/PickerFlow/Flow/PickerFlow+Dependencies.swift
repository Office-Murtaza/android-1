import Swinject
import UIKit

protocol PickerActionSheetDelegate: AnyObject {
  func didSelect(_ item: PickerConfig.PickerItem)
}

extension PickerFlow {
  class Dependencies: Assembly {
    
    func assembleProxyDelegate(container: Container) {
      container.register(PickerProxyDelegate.self) { ioc in
        let delegateProxy = PickerProxyDelegate()
        delegateProxy.delegate = ioc.resolve(PickerDelegate.self)!
        return delegateProxy
        }.inObjectScope(.container)
    }
    
    func assembleActionSheet(container: Container) {
      container.register(UIAlertController.self) { (ioc, config: PickerConfig) in
        let delegate = ioc.resolve(PickerActionSheetDelegate.self)!
        let actionSheet = UIAlertController(title: config.title, message: nil, preferredStyle: .actionSheet)
        
        config.items.forEach { item in
          actionSheet.addAction(UIAlertAction(title: item.itemTitle, style: .default) { [weak delegate] _ in
            delegate?.didSelect(item)
          })
        }
        actionSheet.addAction(UIAlertAction(title: config.cancelTitle, style: .cancel))
        return actionSheet
      }
    }
    
    func assembleImagePicker(container: Container) {
      container.register(UIImagePickerController.self) { (ioc, item: PickerConfig.PickerItem) in
        let imagePicker = UIImagePickerController()
        imagePicker.delegate = ioc.resolve(PickerProxyDelegate.self)!
        imagePicker.allowsEditing = item.editable
        
        switch item.sourceType {
        case .camera:
          imagePicker.sourceType = .camera
        case .library:
          imagePicker.sourceType = .photoLibrary
        }
        
        return imagePicker
        }.inObjectScope(.transient)
    }
    
    func assemble(container: Container) {
      container
        .register(PickerFlowController.self) { ioc in
          let controller = PickerFlowController()
          controller.delegate = ioc.resolve(PickerFlowControllerDelegate.self)
          return controller
        }
        .inObjectScope(.container)
        .implements(PickerActionSheetDelegate.self,
                    PickerDelegate.self)
      
      assembleProxyDelegate(container: container)
      assembleActionSheet(container: container)
      assembleImagePicker(container: container)
    }
  }
}

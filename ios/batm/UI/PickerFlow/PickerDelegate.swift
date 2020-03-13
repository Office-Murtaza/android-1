import UIKit

protocol PickerDelegate: class {
  func didSelect(image: UIImage)
  func didCancel()
}

class PickerProxyDelegate: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
  
  weak var delegate: PickerDelegate?
  
  func imagePickerController(_ picker: UIImagePickerController,
                             didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {

    if let image = info[.editedImage] as? UIImage {
      delegate?.didSelect(image: image)
    } else if let originalImage = info[.originalImage] as? UIImage {
      delegate?.didSelect(image: originalImage)
    } else {
      delegate?.didCancel()
    }
  }
  
  func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
    delegate?.didCancel()
  }
}

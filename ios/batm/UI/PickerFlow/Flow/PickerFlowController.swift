import Foundation
import RxFlow

protocol PickerFlowControllerDelegate: class {
  func didPick(image: UIImage)
}

class PickerFlowController: FlowController {
  weak var delegate: PickerFlowControllerDelegate?
}

extension PickerFlowController: PickerActionSheetDelegate {
  func didSelect(_ item: PickerConfig.PickerItem) {
    step.accept(PickerFlow.Steps.takePhoto(item))
  }
}

extension PickerFlowController: PickerDelegate {
  func didSelect(image: UIImage) {
    complete { [delegate] in
      delegate?.didPick(image: image)
    }
  }
  
  func didCancel() {
    complete {}
  }
}

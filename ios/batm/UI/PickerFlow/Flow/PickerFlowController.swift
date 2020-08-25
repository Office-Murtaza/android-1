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
    delegate?.didPick(image: image)
    step.accept(PickerFlow.Steps.dismiss)
  }
  
  func didCancel() {
    step.accept(PickerFlow.Steps.dismiss)
  }
}

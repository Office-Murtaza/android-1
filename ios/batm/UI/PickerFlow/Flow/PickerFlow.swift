import UIKit
import RxFlow

class PickerFlow: BaseFlow<BTMNavigationController, PickerFlowController> {
  
  override func assemblies() -> [Assembly] {
    return [
      Dependencies()
    ]
  }
  
  enum Steps: Step, Equatable {
    case start(PickerConfig)
    case takePhoto(PickerConfig.PickerItem)
    case dismiss
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handlePickerFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handlePickerFlow(step: Steps) -> NextFlowItems {
    switch step {
    case let .start(config):
      let picker = resolver.resolve(UIAlertController.self, argument: config)!
      view.topViewController?.present(picker, animated: true, completion: nil)
      return .none
    case let .takePhoto(item):
      let picker = resolver.resolve(UIImagePickerController.self, argument: item)!
      view.topViewController?.present(picker, animated: true, completion: nil)
      return .none
    case .dismiss:
      return dismiss()
    }
  }
}

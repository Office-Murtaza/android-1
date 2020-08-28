import Foundation
import RxSwift
import RxFlow

protocol MainFlowControllerDelegate: class {}

class MainFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = MainFlow.Steps.main
  
  weak var delegate: MainFlowControllerDelegate?
  
}

extension MainFlowController: WalletFlowControllerDelegate {}
extension MainFlowController: ATMFlowControllerDelegate {}
extension MainFlowController: SettingsFlowControllerDelegate {}

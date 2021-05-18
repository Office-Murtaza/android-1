import Foundation
import RxSwift
import RxFlow

protocol MainFlowControllerDelegate: AnyObject {}

class MainFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = MainFlow.Steps.main
  
  weak var delegate: MainFlowControllerDelegate?
  
}

extension MainFlowController: WalletFlowControllerDelegate {}
extension MainFlowController: DealsFlowControllerDelegate {}
extension MainFlowController: ATMFlowControllerDelegate {}
extension MainFlowController: SettingsFlowControllerDelegate {}

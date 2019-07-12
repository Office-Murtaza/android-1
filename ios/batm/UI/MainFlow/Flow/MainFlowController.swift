import Foundation
import RxSwift
import RxFlow

protocol MainFlowControllerDelegate: class {}

class MainFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = MainFlow.Steps.coinsBalance
  
  weak var delegate: MainFlowControllerDelegate?
  
}

extension MainFlowController: CoinsBalanceModuleDelegate {}

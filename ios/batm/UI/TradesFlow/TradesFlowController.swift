import RxFlow

protocol TradesFlowControllerDelegate: class {
  func didFinishTradesFlow()
}

class TradesFlowController: FlowController {
  
  weak var delegate: TradesFlowControllerDelegate?
  
}

extension TradesFlowController: TradesModuleDelegate {
  
  func didFinishTrades() {
    delegate?.didFinishTradesFlow()
  }
  
}

import Foundation
import RxSwift
import RxFlow

protocol TransferFlowControllerDelegate: class {}

class TransferFlowController: FlowController {
  var initialStep: Step = TransferFlow.Steps.transfer
    
  weak var delegate: TransferFlowControllerDelegate?
  weak var module: TransferModule?
}

extension TransferFlowController: TransferModuleDelegate {
    func showSendGift(contact: BContact) {
        step.accept(TransferFlow.Steps.sendGift(contact))
    }
}

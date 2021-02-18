import Foundation
import RxSwift
import RxFlow

protocol DealsFlowControllerDelegate: class {}

class DealsFlowController: FlowController, FlowActivator {
    var initialStep: Step = DealsFlow.Steps.deals
    weak var delegate: DealsFlowControllerDelegate?
}

extension DealsFlowController: DealsModuleDelegate {
    func didSelectStaking() {
        step.accept(DealsFlow.Steps.staking)
    }
    
    func didSelectSwap() {
        step.accept(DealsFlow.Steps.swap)
    }
    
    func didSelectTransfer() {
        step.accept(DealsFlow.Steps.transfer)
    }
}

extension DealsFlowController: CoinExchangeModuleDelegate {
    func didFinishCoinExchange() {
        step.accept(DealsFlow.Steps.popToRoot(localize(L.CoinDetails.transactionCreated)))
    }
}

extension DealsFlowController: CoinStakingModuleDelegate {
    func didFinishCoinStaking() {
        step.accept(DealsFlow.Steps.popToRoot(localize(L.CoinDetails.transactionCreated)))
    }
}

extension DealsFlowController: CoinSendGiftModuleDelegate {
    func didFinishCoinSendGift() {
        step.accept(DealsFlow.Steps.popToRoot(localize(L.CoinDetails.transactionCreated)))
    }
}

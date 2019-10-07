import RxFlow

protocol CoinDetailsFlowControllerDelegate: class {
  func didFinishCoinDetailsFlow()
}

class CoinDetailsFlowController: FlowController {
  
  weak var delegate: CoinDetailsFlowControllerDelegate?
  
}

extension CoinDetailsFlowController: CoinDetailsModuleDelegate {
  
  func didFinishCoinDetails() {
    delegate?.didFinishCoinDetailsFlow()
  }
  
  func showWithdrawScreen(for coin: BTMCoin, and coinBalance: CoinBalance) {
    step.accept(CoinDetailsFlow.Steps.withdraw(coin, coinBalance))
  }
  
  func showSendGiftScreen(for coin: BTMCoin, and coinBalance: CoinBalance) {
    step.accept(CoinDetailsFlow.Steps.sendGift(coin, coinBalance))
  }
  
}

extension CoinDetailsFlowController: CoinWithdrawModuleDelegate {
  
  func didFinishCoinWithdraw() {
    step.accept(CoinDetailsFlow.Steps.pop)
  }
  
}

extension CoinDetailsFlowController: CoinSendGiftModuleDelegate {
  
  func didFinishCoinSendGift() {
    step.accept(CoinDetailsFlow.Steps.pop)
  }
  
}

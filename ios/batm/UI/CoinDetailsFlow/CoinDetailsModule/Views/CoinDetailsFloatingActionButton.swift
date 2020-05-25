import UIKit
import RxSwift
import RxCocoa
import JJFloatingActionButton

class CoinDetailsFloatingActionButton: ReactiveCompatible, JJFloatingActionButtonDelegate {
  
  let didTapDepositRelay = PublishRelay<Void>()
  let didTapWithdrawRelay = PublishRelay<Void>()
  let didTapSendGiftRelay = PublishRelay<Void>()
  let didTapSellRelay = PublishRelay<Void>()
  let didTapExchangeRelay = PublishRelay<Void>()
  let didTapTradesRelay = PublishRelay<Void>()
  
  let view = JJFloatingActionButton()
  
  init() {
    setupUI()
  }
  
  private func setupUI() {
    view.buttonDiameter = 56
    view.overlayView.backgroundColor = UIColor(white: 0, alpha: 0.6)
    view.buttonImage = UIImage(named: "fab_plus")
    view.buttonColor = .ceruleanBlue
    view.buttonImageColor = .white
    
    let fabCancelImage = UIImage(named: "fab_cancel")
    fabCancelImage.flatMap { view.buttonAnimationConfiguration = .transition(toImage: $0) }
    view.itemAnimationConfiguration = .slideIn(withInterItemSpacing: 15)
    
    view.layer.shadowColor = UIColor.black.cgColor
    view.layer.shadowOffset = CGSize(width: 0, height: 5)
    view.layer.shadowOpacity = Float(0.2)
    view.layer.shadowRadius = CGFloat(5)
    
    view.configureDefaultItem { item in
      item.titleLabel.font = .systemFont(ofSize: 14, weight: .medium)
      item.titleLabel.textColor = .white
      item.buttonColor = .ceruleanBlue
      item.buttonImageColor = .white
      
      item.layer.shadowColor = UIColor.black.cgColor
      item.layer.shadowOffset = CGSize(width: 0, height: 5)
      item.layer.shadowOpacity = Float(0.2)
      item.layer.shadowRadius = CGFloat(5)
    }
    
    view.addItem(title: localize(L.CoinDetails.deposit), image: UIImage(named: "fab_deposit")) { [unowned self] _ in
      self.didTapDepositRelay.accept(())
    }
    view.addItem(title: localize(L.CoinDetails.withdraw), image: UIImage(named: "fab_withdraw")) { [unowned self] _ in
      self.didTapWithdrawRelay.accept(())
    }
    view.addItem(title: localize(L.CoinDetails.sendGift), image: UIImage(named: "fab_send_gift")) { [unowned self] _ in
      self.didTapSendGiftRelay.accept(())
    }
    view.addItem(title: localize(L.CoinDetails.sell), image: UIImage(named: "fab_sell")) { [unowned self] _ in
      self.didTapSellRelay.accept(())
    }
    view.addItem(title: localize(L.CoinDetails.c2cExchange), image: UIImage(named: "fab_exchange")) { [unowned self] _ in
      self.didTapExchangeRelay.accept(())
    }
    view.addItem(title: localize(L.CoinDetails.trade), image: UIImage(named: "fab_trade")) { [unowned self] _ in
      self.didTapTradesRelay.accept(())
    }
    
    view.delegate = self
  }
  
  func floatingActionButtonWillOpen(_ button: JJFloatingActionButton) {
    button.buttonColor = .white
    button.buttonImageColor = .ceruleanBlue
  }
  
  func floatingActionButtonWillClose(_ button: JJFloatingActionButton) {
    button.buttonColor = .ceruleanBlue
    button.buttonImageColor = .white
  }
  
}

extension Reactive where Base == CoinDetailsFloatingActionButton {
  var depositTap: Driver<Void> {
    return base.didTapDepositRelay.asDriver(onErrorDriveWith: .empty())
  }
  var withdrawTap: Driver<Void> {
    return base.didTapWithdrawRelay.asDriver(onErrorDriveWith: .empty())
  }
  var sendGiftTap: Driver<Void> {
    return base.didTapSendGiftRelay.asDriver(onErrorDriveWith: .empty())
  }
  var sellTap: Driver<Void> {
    return base.didTapSellRelay.asDriver(onErrorDriveWith: .empty())
  }
  var exchangeTap: Driver<Void> {
    return base.didTapExchangeRelay.asDriver(onErrorDriveWith: .empty())
  }
  var tradesTap: Driver<Void> {
    return base.didTapTradesRelay.asDriver(onErrorDriveWith: .empty())
  }
}

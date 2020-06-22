import UIKit
import RxSwift
import RxCocoa
import JJFloatingActionButton

class FloatingActionButton: ReactiveCompatible, JJFloatingActionButtonDelegate {
  
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
